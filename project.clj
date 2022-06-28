(defproject com.github.strojure/jmustache "1.1.1-SNAPSHOT"
  :description "Clojure adapter to jmustache library."
  :url "https://github.com/strojure/jmustache"
  :license {:name "The MIT License" :url "http://opensource.org/licenses/MIT"}

  :dependencies [[com.samskivert/jmustache "1.15"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]]}}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo" :sign-releases false}]])
