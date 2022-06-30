# jmustache

Clojure adapter to [jmustache](https://github.com/samskivert/jmustache) library.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.strojure/jmustache.svg)](https://clojars.org/com.github.strojure/jmustache)

## Features

- Fetch context data from Clojure persistent maps with keyword keys.
- `:throw-missing?` compiler option to catch errors during development.
- Easy declaration of mustache lambdas.
- Fast rendering of templates.

## Usage

```clojure
(ns readme.usage
  (:require [strojure.jmustache.core :as jmustache]))

(def ^:private my-source
  "
Hello {{name}}
You have just won {{value}} dollars!
{{#in-ca}}
Well, {{taxed-value}} dollars, after taxes.
{{/in-ca}}
  ")

(def ^:private my-context
  {:name "Chris",
   :value 10000,
   :taxed-value (- 10000 (* 10000 0.4)),
   :in-ca true})


;;; Ad-hoc render template.

(-> (jmustache/mustache-compiler)
    (jmustache/execute-source my-source my-context)
    (print))

;Hello Chris
;You have just won 10000 dollars!
;Well, 6000.0 dollars, after taxes.

(-> (jmustache/mustache-compiler)
    (jmustache/execute-source my-source my-context))
#_=> "\nHello Chris\nYou have just won 10000 dollars!\nWell, 6000.0 dollars, after taxes.\n  "

;Evaluation count : 197880 in 6 samples of 32980 calls.
;             Execution time mean : 3,163926 µs
;    Execution time std-deviation : 283,878514 ns
;   Execution time lower quantile : 2,934028 µs ( 2,5%)
;   Execution time upper quantile : 3,561389 µs (97,5%)
;                   Overhead used : 7,600431 ns


;;; Render pre-compiled template.

(def ^:private compile-fn
  (jmustache/execute-source-fn (jmustache/mustache-compiler) my-source))

(compile-fn my-context)
#_=> "\nHello Chris\nYou have just won 10000 dollars!\nWell, 6000.0 dollars, after taxes.\n  "

;Evaluation count : 780606 in 6 samples of 130101 calls.
;             Execution time mean : 807,030899 ns
;    Execution time std-deviation : 21,954802 ns
;   Execution time lower quantile : 779,365854 ns ( 2,5%)
;   Execution time upper quantile : 834,735336 ns (97,5%)
;                   Overhead used : 7,600431 ns


;;; Context map with string keys using default fetcher.

(def ^:private my-context-strs
  {"name" "Chris",
   "value" 10000,
   "taxed-value" (- 10000 (* 10000 0.4)),
   "in-ca" true})

(-> (jmustache/mustache-compiler :map-fetcher nil)
    (jmustache/execute-source my-source my-context-strs)
    (print))

;Hello Chris
;You have just won 10000 dollars!
;Well, 6000.0 dollars, after taxes.


;;; Context map with string and keyword keys.

(def ^:private my-context-mixed
  {:name "Chris",
   "value" 10000,
   "taxed-value" (- 10000 (* 10000 0.4)),
   :in-ca true})

(-> (jmustache/mustache-compiler :map-fetcher (jmustache/mixed-map-fetcher))
    (jmustache/execute-source my-source my-context-mixed)
    (print))

;Hello Chris
;You have just won 10000 dollars!
;Well, 6000.0 dollars, after taxes.


;;; Throw exception about missing values.

(-> (jmustache/mustache-compiler :throw-missing? true)
    (jmustache/execute-source my-source (dissoc my-context :name)))

;Execution error (MustacheException$Context) at com.samskivert.mustache.Template/checkForMissing (Template.java:344).
;No method or field with name 'name' on line 2

(-> (jmustache/mustache-compiler :throw-missing? false)
    (jmustache/execute-source my-source (dissoc my-context :name))
    (print))

;Hello 
;You have just won 10000 dollars!
;Well, 6000.0 dollars, after taxes.
```
## Q&A

### What's the difference to https://github.com/fhd/clostache?

Java implementations offer much better performance than clostache.  
The https://github.com/spullara/mustache.java is fast and has adapter but behaves buggy in my use cases.  
Probably there are some feature differences between clostache and jmustache.
