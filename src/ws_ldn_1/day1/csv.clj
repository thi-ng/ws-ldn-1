(ns ws-ldn-1.day1.csv
  (:require
    [clojure.java.io :as io]
    [clojure.data.csv :as csv]
    [thi.ng.strf.core :as f]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :refer [vec2]]))

(defn load-csv-resource
  "Takes CSV source (path, URI or stream) and returns parsed CSV as vector of rows"
  [path]
  (let [data (-> path
                 (io/reader)
                 (csv/read-csv :separator \,))]
    (println (count data) "rows loaded")
    data))

(defn build-column-index
  "Takes a set of columns to be indexed and first row of CSV (column headers).
  Returns map of {id col-name, ...} for all matched columns."
  [wanted-columns csv-columns]
  (->> csv-columns
       (map (fn [i x] [i x]) (range))
       (filter #(wanted-columns (second %)))
       (into {})))

(defn transform-csv-row
  "Generic CSV row transformer. Takes a column index map as returned
  by build-column-index and single CSV row vector, returns map of
  desired columns with empty cols removed. Column names are also
  turned into keywords."
  [col-idx keep-cols row]
  (->> row
       (map-indexed (fn [i x] [i x]))               ;; add col # to each value
       (filter (fn [[i]] (keep-cols i)))            ;; only keep indexed columns
       (map (fn [[i x]] [(keyword (col-idx i)) x])) ;; form pairs of [:col-name val]
       (remove (fn [x] (empty? (second x))))        ;; remove all cols w/ empty vals
       (into {})))                                  ;; turn into hash-map

(defn transform-lat-lon
  "Takes a single airport map and parses string lat/lon keys as numbers.
  Also inject new key :lonlat-point as 2d vector.
  Returns updated airport."
  [airport]
  (let [airport' (-> airport
                     (update :latitude_deg f/parse-float)
                     (update :longitude_deg f/parse-float))]
    (assoc airport'
           :lonlat-point (vec2 (:longitude_deg airport')
                               (:latitude_deg airport')))))
  
(defn load-airports
  [path cols]
  (let [airports  (load-csv-resource path)
        col-idx   (build-column-index cols (first airports))
        keep-cols (set (keys col-idx))
        airports  (map #(transform-csv-row col-idx keep-cols %) (rest airports))
        airports  (map #(transform-lat-lon %) airports)
        airports  (remove #(> (Math/abs (:latitude_deg %)) 88) airports)]
    airports))

(def airports
  (load-airports
    (io/resource "airports.csv")
    #{"name" "latitude_deg" "longitude_deg" "iso_country" "iata_code"}))
