(ns web.select
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.stacktrace :only [wrap-stacktrace ]]
        [ring.util.response :only [response file-response redirect]]
        [clojure.walk :only [walk]]
        [net.cgrand.moustache :only [app delegate]])
  (:require [net.cgrand.enlive-html :as h]))


;;To get all public functions in file order
#_(map first (sort-by (comp :line meta second) (ns-publics 'clojure.walk)))

(def enlive-functions (set (map first (ns-publics 'net.cgrand.enlive-html))))

(defn fullify [form]
  (clojure.walk/postwalk
   #(if (and (symbol? %) (enlive-functions %))
      (symbol (str "net.cgrand.enlive-html/" (name %)))
      %)
   form))

(defn str->clj
  [s]
  (if (string? s)
    (eval `~(fullify (read-string s)))
    s))

(defn clj->str [c]
  (cond
    (string? c) c
    (nil? c) ""
    :else (pr-str c)))


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
  ""
  [selector text]
;;  (println "select-nodes--> " selector)
  (nodes->str
     (h/select (source->nodes text)
               (str->clj selector))))

(defn extract-nodes [selector extractor text]
  ((str->clj extractor)
   (h/select (source->nodes text) (str->clj selector))))

(defn transform-nodes
  [selector trans text]
  (nodes->str
   (h/transform (h/html-snippet text)
                (str->clj selector)
                (str->clj trans))))

(def examples
  [{:id "simple" :title "Simple" :selector [:a]
    :source "<span><a>llll</a></span>"}
   {:id "content" :title "Content" :selector [:a] :transform "(content \"hello\")"
    :source "<span><a>llll</a></span>"}
   {:id "fragment" :title "Fragment" :selector "{[:h1] [:p]}"
    :source "<div><h1>title</h1><h2>Sub title</h2><p>some text</p></div><h1>Another Title</h1>"}
   {:id "tagattr" :title "Tag/Attr"
    :selector  "[[:a (attr= :href \"/\")]]"
    :source "<div>><a>ll1</a></li><li><a href=\"/\">index</a></div>"}
   {:id "attrstart" :title "attr-starts"
    :selector  "[[:a (attr-starts :href \"/\")]]"
    :source "<div><a>ll1</a></li><li><a href=\"/\">index</a></div>"}
   {:id "4pb" :title "4pb scraping"
    :selector "[:div#prob-title]" :transform "texts"
    :source "http://4clojure.com/problem/111"}
   {:id "takenotwhile" :title "take not while"
    :description "any elements in div before ul"
    :selector "[:div (pred #(not (= :ul (:tag %))))]"
    :source "<div><span>some text</span><a>link</a><br><ul><li>item1</li></ul></div>"}
   ])

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


;; misc middleware
(defn haz? [coll element] (boolean (some (conj #{} element) coll)))
;;use pr-str instead of json-pr

(defn wrap-logging
  "Ring middleware for request logging.
   Why JSON: http://journal.paul.querna.org/articles/2011/12/26/log-for-machines-in-json/
   Options:
    :output -- :stdout, :stderr, a string (file path) or a function (for log systems)
               :stdout is default
    :status-filter -- a collection of status codes to log responses with or a predicate
                      (eg. #(> % 399)). nil is default
    :keys-filter -- which keys of (merge req res) to log"
  [handler {:keys [output status-filter keys-filter]
            :or {output :stdout
                 status-filter nil
                 keys-filter [:status :uri :remote-addr :request-method]}}]
  (fn [req]
    (let [res (handler req)
          status-filter (cond
                          (nil? status-filter) (fn [s] true)
                          (coll? status-filter) #(haz? status-filter %)
                          (fn? status-filter) status-filter)
          logger (cond
                   (= output :stdout) println
                   (= output :stderr) #(binding [*out* *err*]
                                         (println %))
                   (string? output) #(spit output (str % "\n") :append true)
                   (fn? output) output)
          entry (-> (select-keys (merge req res) keys-filter)
                    pr-str
                    (.replace "\\" ""))]
      (if (status-filter (:status res)) (logger entry))
      res)))

;; misc enlive utils
(defn render [t]
  (apply str t))

(def render-to-response
  (comp response render))

;; from https://$github.com/swannodette/enlive-tutorial
(defn render-request [afn & args]
  (fn [req] (render-to-response (apply afn args))))

(defn prepend-attrs [att prefix]
  (fn[node] (update-in node [:attrs att] (fn[v] (str prefix v)))))

(defmacro mydeftemplate
  "Same as deftemplate but make resources url absolute ( prepend / )"
  [name source args & forms]
  `(h/deftemplate ~name ~source ~args
     [[:link (h/attr= :rel "stylesheet")]] (prepend-attrs :href "/")
     ~@forms))

;;

(h/defsnippet nav-item "select.html" [:#navexamples [:li (h/nth-of-type 2)]]
  [{:keys [title id]}]
  [:a] (h/do-> (h/set-attr :href (str "/" id))
               (h/content title)))

(mydeftemplate index "select.html"
               [{:keys [error trace source transform selector selection]}]
               [:#navexamples] (h/content (mapcat nav-item examples))
               [:#i_selector] (h/set-attr :value (clj->str selector))
               [:#i_error] (if error (h/content error) (h/substitute ""))
               [:#i_trace] (if (and error trace)
                             (h/content (clojure.string/join "\n" trace))
                             (h/substitute ""))
               [:#i_transform] (h/set-attr :value transform)
               [:#i_source] (h/content source)
               [:#l_transform] (h/content transform)
               [:#l_selector] (h/content (clj->str selector))
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
  (render-to-response (index (append-selection params))))

(def routes
  (app (wrap-file "resources")
       (wrap-params)
       (wrap-keyword-params)
       (wrap-stacktrace)
       (wrap-logging {:keys-filter [:status :uri :params :request-method]})
       [ id & ] {:get (render-request index (append-selection (find-example id)))
                 :post process-selection}
       ))

(defn start [ & [port & options]]
  (run-jetty (var routes) {:port (or port 8080) :join? false}))

(defn -main []
  (let [port (try (Integer/parseInt (System/getenv "PORT"))
                  (catch  Throwable t 8080))]
    (start port)))



