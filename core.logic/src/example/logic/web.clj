(ns example.logic.web
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.stacktrace :only [wrap-stacktrace ]]
        [ring.util.response :only [response file-response redirect]]
        [clojure.walk :only [walk]]
        [net.cgrand.moustache :only [app delegate]])
  (:require [clojure.string :as s]
            [net.cgrand.enlive-html :as h]
            [clojure.core.logic :as l])
  (:gen-class))


;;To get all public functions in file order
#_(map first (sort-by (comp :line meta second) (ns-publics 'clojure.walk)))

(def logic-functions (set (map first (ns-publics 'clojure.core.logic))))

(defn fullify [form]
  (clojure.walk/postwalk
   #(if (and (symbol? %) (logic-functions %))
      (symbol (str "clojure.core.logic/" (name %)))
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

(def examples
  [{:id "simple" :title "Simple" :source "(== 3 q)"}
   {:id "appendo" :title "Appendo"
    :source "(appendo [1 2] q [1 2 3 4 5])" :usage ["appendo"]}]
  )

(def sections
  [])

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

;; from https://github.com/swannodette/enlive-tutorial
(defn render-request [afn & args]
  (fn [req] (render-to-response (apply afn args))))

(defn prepend-attrs [att prefix]
  (fn[node] (update-in node [:attrs att] (fn[v] (str prefix v)))))

;; Enlive Template
(defmacro mydeftemplate
  "Same as deftemplate but make resources url absolute ( prepend / )"
  [name source args & forms]
  `(h/deftemplate ~name ~source ~args
     [[:link (h/attr= :rel "stylesheet")]] (prepend-attrs :href "/")
     ~@forms))

(h/defsnippet nav-item "logic.html" [:#navexamples [:li (h/nth-of-type 2)]]
  [{:keys [title id]}]
  [:a] (h/do-> (h/set-attr :href (str "/" id))
               (h/content title)))

(mydeftemplate index "logic.html"
               [{:keys [error trace description source usage solution]}]
               [:#navexamples] (h/content (mapcat nav-item examples))
               [:#i_source] (h/content (clj->str source))
               [:#i_error] (if error (h/content error) (h/substitute ""))
               [:#i_desc] (if description (h/content description) (h/substitute ""))
               [:#i_trace] (if (and error trace)
                             (h/content (s/join "\n" trace))
                             (h/substitute ""))
               [:#l_source] (h/content source)
               [:#l_solution] (h/content
                               (s/join "," (map #(if (seq? %) (vec %) %)
                                                solution))))

;; Business Logic
(defn find-example [id]
  (first (filter #(= id (:id %)) examples)))

(defn solve
  "Returns solutions of a core.logic program"
  [s limit]
  (eval `(l/run ~limit [~'q] ~(fullify (read-string s)))))

(defn run-example [{:keys [source limit] :or {limit 10} :as example}]
  (try
    (assoc example :solution (solve source 10))
    (catch Throwable t (assoc example
                         :error (.getMessage t)
                         :trace (.getStackTrace t)))))

(defn process-example [{params :params :as req}]
  (render-to-response (index (run-example params))))

;; Routing
(def routes
  (app (wrap-file "resources")
       (wrap-params)
       (wrap-keyword-params)
       (wrap-stacktrace)
       (wrap-logging {:keys-filter [:status :uri :params :request-method]})
       [ id & ] {:get (render-request index (run-example (find-example id)))
                 :post process-example}
       ))

;; Start server
(defn start [ & [port & options]]
  (run-jetty (var routes) {:port (or port 8080) :join? false}))

(defn -main [& [port]]
  (let [port (try
               (Integer/parseInt
                (some identity
                      [port (System/getenv "VMC_APP_PORT") (System/getenv "PORT")]))
               (catch  Throwable t 8080))]
    (start port)))
