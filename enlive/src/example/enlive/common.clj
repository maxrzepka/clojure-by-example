(ns example.enlive.common
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

(defn all-fns [s]
  (set (map first (ns-publics (symbol s)))))

;; TODO some refactoring needed to move it outside common namespace
(def names {:h "net.cgrand.enlive-html" })

(def lookup-namespace
  (into {} (mapcat (fn [[k v]] (map (fn [f] [f v]) (all-fns v))) names)))

(defn find-namespace [s]
  (if (symbol? s)
    (if (namespace s)
      ;; symbol with namespace
      (names (-> s namespace first str keyword))
      (lookup-namespace s))))

(defn fullify [form]
  (clojure.walk/postwalk
   (fn [s]
     (if-let [ns1 (find-namespace s)]
             (symbol (str ns1 "/" (name s)))
             s))
   form))

(defn str->clj
  [s]
  (eval (fullify (if (string? s) (read-string s) s))))

(defn clj->str [c]
  (cond
    (string? c) c
    (nil? c) ""
    :else (pr-str c)))


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

