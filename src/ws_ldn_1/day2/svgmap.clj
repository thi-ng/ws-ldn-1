(ns ws-ldn-1.day2.svgmap
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.svg.core :as svg]
    [ws-ldn-1.day2.mercator :as proj]))

(def latlon-bounds [-180 180 82 -82])

(def cities
  [[-0.13 51.1]             ;; London
   [-71.058880 42.360082]   ;; Boston
   [12.496366 41.902783]    ;; Rome
   [174.763332 -36.848460]] ;; Auckland
  )

(defn svg-image
  [x y w h src]
  [:image {:x x :y y :width w :height h "xlink:href" src}])

(->> (svg/svg
       {:width 1029 :height 873}
       (svg-image 0 0 1029 873 "https://upload.wikimedia.org/wikipedia/commons/f/f4/Mercator_projection_SW.jpg")
       (map #(svg/circle (proj/mercator-in-rect % latlon-bounds 1029 873) 3 {:fill "red"}) cities))
     (svg/serialize)
     (spit "map.svg"))