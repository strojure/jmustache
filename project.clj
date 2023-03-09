(defproject com.github.strojure/jmustache "1.0.1-07"
  :description "Clojure adapter to jmustache library."
  :url "https://github.com/strojure/jmustache"
  :license {:name "The Unlicense" :url "https://unlicense.org"}

  :dependencies [[com.samskivert/jmustache "1.15"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]]}
             :dev {:source-paths ["doc"]}}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo" :sign-releases false}]])
