(ns strojure.jmustache.core
  (:import (clojure.lang APersistentMap Keyword)
           (com.samskivert.mustache DefaultCollector Mustache Mustache$Collector Mustache$Compiler
                                    Mustache$Lambda Mustache$VariableFetcher Template Template$Fragment)
           (java.io Reader Writer)
           (java.util.concurrent ConcurrentHashMap)
           (java.util.function Function)))

(set! *warn-on-reflection* true)

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn keyword-map-fetcher
  "Returns variable fetcher which gets map value by keyword."
  []
  (reify Mustache$VariableFetcher
    (get [_ m k]
      (m (Keyword/intern ^String k) Template/NO_FETCHER_FOUND))))

(defn mixed-map-fetcher
  "Returns variable fetcher which gets map value by keyword then by string."
  []
  (reify Mustache$VariableFetcher
    (get [_ m k]
      (let [v (m (Keyword/intern ^String k) Template/NO_FETCHER_FOUND)]
        (if (identical? v Template/NO_FETCHER_FOUND)
          (m k Template/NO_FETCHER_FOUND)
          v)))))

(defn- clojure-map?
  [x]
  (instance? APersistentMap x))

(defn clojure-collector
  "Returns jmustache's collector which works with clojure data structures.
  The `:map-fetcher` option defines map-fetcher for getting values from clojure
  maps, default is [[keyword-map-fetcher]]."
  [{:keys [map-fetcher] :or {map-fetcher (keyword-map-fetcher)}}]
  (let [collector (DefaultCollector.)]
    (reify Mustache$Collector
      (toIterator [_ value] (when-not (clojure-map? value)
                              (.toIterator collector value)))
      (createFetcher [_ ctx nom] (or (when (clojure-map? ctx) map-fetcher)
                                     (.createFetcher collector ctx nom)))
      (createFetcherCache [_] (.createFetcherCache collector)))))

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn mustache-compiler
  "Returns instance of jmustache's compiler. The `:map-fetcher` option defines
  map-fetcher for getting values from clojure maps, default is
  [[keyword-map-fetcher]]. The `:throw-missing?` option defines if exception
  should be thrown for missing values during render of compiled templates."
  {:arglists '([{:keys [map-fetcher, throw-missing?]}])}
  ^Mustache$Compiler
  [& {:keys [throw-missing?] :as opts}]
  (-> (Mustache/compiler)
      (.withCollector (clojure-collector opts))
      (.defaultValue "")
      (cond-> throw-missing? (.nullValue ""))))

(defn compile-source
  "Returns compiled template from String or Reader source."
  ^Template
  [compiler source]
  (if (string? source)
    (.compile ^Mustache$Compiler compiler ^String source)
    (.compile ^Mustache$Compiler compiler ^Reader source)))

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn execute-template
  "Executes mustache template with context and optional writer."
  ([template context] (.execute ^Template template context))
  ([template context out] (.execute ^Template template context out)))

(defn execute-template-fn
  "Returns function to execute mustache template with context and optional
  writer."
  [template]
  (fn
    ([context] (execute-template template context))
    ([context out] (execute-template template context out))))

(defn execute-source
  "Executes mustache template from the String or Reader source with context and
  optional writer."
  ([compiler source context]
   (-> (compile-source compiler source) (execute-template context)))
  ([compiler source context out]
   (-> (compile-source compiler source) (execute-template context out))))

(defn execute-source-fn
  "Returns function to execute mustache template from the String or Reader
  source with context and optional writer."
  [compiler source]
  (execute-template-fn (compile-source compiler source)))

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn lambda
  "Converts clojure function to mustache lambda. If `compiler` provided then
  result of function application is rendered as mustache template with the
  current context."
  ([f]
   (reify Mustache$Lambda
     (execute [_, frag, out]
       (.write ^Writer out (str (f (.execute ^Template$Fragment frag)))))))
  ([f, compiler]
   (assert (instance? Mustache$Compiler compiler))
   (let [cache (ConcurrentHashMap.)
         compile (reify Function (apply [_ v] (.compile ^Mustache$Compiler compiler ^String v)))]
     (reify Mustache$Lambda
       (execute [_, frag, out]
         (let [text (str (f (.execute ^Template$Fragment frag)))
               template (-> cache (.computeIfAbsent text compile))]
           (.executeTemplate ^Template$Fragment frag template out)))))))

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
