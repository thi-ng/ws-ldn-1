(ns ws-ldn-1.day1.people
  "Basic I/O using EDN format and data transformation example
  with thread-last macro (->>)."
  (:require
    [clojure.java.io :as io]           ;; require namespaces with alias
    [clojure.edn :as edn]
    [clojure.pprint :refer [pprint]])) ;; import ns & allow referring to pprint directly

;; load people specs from resource file

(def people-raw
  (->> "day1/people.edn"
    (io/resource)             ;; convert to resource URI
    (slurp)                   ;; read as string
    (edn/read-string)))       ;; parse EDN format

;; or alternatively directly parse from input stream
;; better when loading larger data

(def people-raw
  (->> "day1/people.edn"
    (io/resource)
    (io/reader)               ;; create java.io.Reader instance from URI
    (java.io.PushbackReader.) ;; wrap in PushbackReader
    (edn/read)))              ;; read from stream

(defn person-map
  "Converts single person spec (vector) into hash-map.
  Uses vector destructuring for function arguments"
  [[name langs city]]
  {:name name :langs (set langs) :city city})

;; transform vector of raw specs into vector of person maps

(def people (mapv person-map people-raw))

;; alternatively use zipmap to achieve same effect
;; without need for person-map fn
;; (requires extra step to convert :langs into a set)

(def people
  (->> people-raw
       (map (fn [p] (zipmap [:name :langs :city] p))) ;; combine key & val seq into hash-map
       (map (fn [p] (update p :langs set)))           ;; apply (set) fn to :langs value for each item
       (vec)))                                        ;; convert final lazy seq to vector

;; instead of (vec) we could also have used (mapv ...) for the previous step...
;; http://conj.io/store/v1/org.clojure/clojure/1.7.0/clj/clojure.core/mapv

;; use Clojure's pretty printer to show any nested data

(pprint people)

;; write converted data to file in project root dir

(spit "people-converted.edn" people)