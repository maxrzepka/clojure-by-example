(ns example.enlive.web
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.stacktrace :only [wrap-stacktrace ]]
        [ring.util.response :only [response file-response redirect]]
        [clojure.walk :only [walk]]
        [net.cgrand.moustache :only [app delegate]]
        [example.enlive.common :as c])
  (:require [net.cgrand.enlive-html :as h])
  (:gen-class))

;;To get all public functions in file order
#_(map first (sort-by (comp :line meta second) (ns-publics 'clojure.walk)))

(def enlive-functions (set (map first (ns-publics 'net.cgrand.enlive-html))))

#_(defn fullify1 [form]
  (clojure.walk/postwalk
   #(if (and (symbol? %) (enlive-functions %))
      (symbol (str "net.cgrand.enlive-html/" (name %)))
      %)
   form))

(defn nodes->str [nodes]
  (if (string? nodes) nodes
      (apply str (h/emit* (h/flatten-nodes-coll nodes)))))

(defn snippet? [text]
  (not (.startsWith text "http://")))

(defn source->nodes [source]
  (if (.startsWith source "http://")
                   (h/html-resource (java.net.URL. source))
                   (h/html-snippet source)))

(defn select-nodes
  [selector text]
;;  (println "select-nodes--> " selector)
  (nodes->str
     (h/select (source->nodes text)
               (c/str->clj selector))))

(defn extract-nodes [selector extractor text]
  ((c/str->clj extractor)
   (h/select (source->nodes text) (c/str->clj selector))))

(defn transform-nodes
  [selector trans text]
  (nodes->str
   (h/transform (h/html-snippet text)
                (c/str->clj selector)
                (c/str->clj trans))))

(def examples
 (read-string (slurp "resources/enlive.clj")))

;;how to get any nodes until ul  [:div (zip-pred #(not (= :ul (:tag %))))]

(def sections
  [{:code "selector" :title "Selectors"
    :description ["Syntax similar to CSS to extract nodes from HMTL page.
A selector is a vector of selector step : " [:div :a] " is selector with
2 selector-steps and means any a element inside a div element. "
 :> " is CSS child combinator "]     :sections [{:code "union" :description [{}]}]}
   {:code "transformer" :title "Transformers"
    :description "Functions for node transformation"}
   ])

;;

(h/defsnippet nav-item "enlive.html" [:#navexamples [:li (h/nth-of-type 2)]]
  [{:keys [title id]}]
  [:a] (h/do-> (h/set-attr :href (str "/" id))
               (h/content title)))

(mydeftemplate index "enlive.html"
               [{:keys [error trace description source transform selector selection]}]
               [:#navexamples] (h/content (mapcat nav-item examples))
               [:#i_selector] (h/set-attr :value (c/clj->str selector))
               [:#i_error] (if error (h/content error) (h/substitute ""))
               [:#i_desc] (if description (h/content description) (h/substitute ""))
               [:#i_trace] (if (and error trace)
                             (h/content (clojure.string/join "\n" trace))
                             (h/substitute ""))
               [:#i_transform] (h/set-attr :value transform)
               [:#i_source] (h/content source)
               [:#l_transform] (h/content transform)
               [:#l_selector] (h/content (c/clj->str selector))
               [:#l_selection] (h/content selection)
               )

(defn find-example [id]
  (first (filter #(= id (:id %)) examples)))

(defn append-selection
  [{:keys [selector transform source] :as params}]
  (try (let [selection (if (seq transform)
                         (if (snippet? source)
                           (transform-nodes selector transform source)
                           (extract-nodes selector transform source))
                         (select-nodes selector source))]
         (assoc params :selection selection))
       (catch Throwable t (assoc params
                            :error (.getMessage t)
                            :trace (.getStackTrace t)))))


(defn process-selection [{params :params :as req}]
;;  (println "process-selection--> " (pr-str params))
  (c/render-to-response (index (append-selection params))))

(def routes
  (app (wrap-file "resources")
       (wrap-params)
       (wrap-keyword-params)
       (wrap-stacktrace)
       (c/wrap-logging {:keys-filter [:status :uri :params :request-method]})
       [ id & ] {:get (c/render-request index (append-selection (find-example id)))
                 :post process-selection}
       ))

(defn start [ & [port & options]]
  (run-jetty (var routes) {:port (or port 8080) :join? false}))

(defn -main [& [port]]
  (let [port (try
               (Integer/parseInt
                (some identity
                      [port (System/getenv "VMC_APP_PORT") (System/getenv "PORT")]))
               (catch  Throwable t 8080))]
    (start port)))
