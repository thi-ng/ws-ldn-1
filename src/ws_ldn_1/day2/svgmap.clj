(ns ws-ldn-1.day2.svgmap
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.svg.core :as svg]
    [ws-ldn-1.day2.mercator :as proj]
    [ws-ldn-1.day1.csv :as data]))

;; map image size
(def map-width 1029)
(def map-height 873)

(def latlon-bounds
  "Angular bounds for mercator map projection"
  [-180 180 82 -82])

(defn svg-image
  "Produces an SVG bitmap image element in hiccup format"
  [x y w h src]
  [:image {:x x :y y :width w :height h "xlink:href" src}])

(defn map-airports
  "Takes a coll of airports, maps their location using mercator projection
  and returns lazyseq of SVG circles (as hiccup)"
  [color w h r airports]
  (svg/group
    {:fill color}
    (->> airports
         (map #(proj/mercator-in-rect (:lonlat-point %) latlon-bounds w h))
         (map #(svg/circle % r)))))

(defn airport-map
  "Takes an output filepath, image width, height and arbitrary number of SVG elements.
  Exports SVG map w/ Wikipedia earth map as background. Returns nil"
  [path w h & body] 
  (->> (svg/svg
         {:width w :height h}
         (svg-image 0 0 w h "https://upload.wikimedia.org/wikipedia/commons/f/f4/Mercator_projection_SW.jpg")
         body)
       (svg/serialize)
       (spit path)))

(airport-map 
  "airports.svg" map-width map-height
  (map-airports "rgba(0,255,255,0.5)" map-width map-height 0.8 (filter (complement :iata_code) data/airports))
  (map-airports "rgba(255,0,255,0.5)" map-width map-height 0.8 (filter :iata_code data/airports)))