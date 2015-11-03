(ns ws-ldn-1.day2.svgmap
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.svg.core :as svg]
    [ws-ldn-1.day2.mercator :as proj]
    [ws-ldn-1.day1.csv :as airports]
    ))

(def latlon-bounds [-180 180 82 -82])

(defn svg-image
  [x y w h src]
  [:image {:x x :y y :width w :height h "xlink:href" src}])

(defn airport-map
  [path color airports] 
  (->> (svg/svg
         {:width 1029 :height 873}
         (svg-image 0 0 1029 873 "https://upload.wikimedia.org/wikipedia/commons/f/f4/Mercator_projection_SW.jpg")
         (map
           #(svg/circle
              (proj/mercator-in-rect (:lonlat-point %) latlon-bounds 1029 873)
              0.8 {:fill color})
           airports))
       (svg/serialize)
       (spit path)))

(airport-map "map-all.svg" "red" airports/airports)
(airport-map "map-iata.svg" "red" (filter :iata_code airports/airports))
(airport-map "map-small.svg" "red" (filter (complement :iata_code) airports/airports))