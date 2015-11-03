(ns ws-ldn-1.day2.mercator
  (:require
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :refer [vec2 vec3]]
   [thi.ng.math.core :as m]))

(def ^:const EARTH_RADIUS_MINOR 6356752.3142)
(def ^:const EARTH_RADIUS_MAJOR 6378137.0)

(defn mercator
  "Takes a longitude and latitude coordinate and returns mapped point."
  [lon lat]
  (let [lat    (m/clamp lat -89.5 89.5)
        rt     (/ EARTH_RADIUS_MINOR EARTH_RADIUS_MAJOR)
        es     (- 1.0 rt)
        ec     (Math/sqrt es)
        phi    (m/radians lat)
        sinphi (Math/sin phi)
        con    (* ec sinphi)
        con    (Math/pow (/ (- 1.0 con) (+ 1.0 con)) (* ec 0.5))
        ts     (/ (Math/tan (/ (- m/HALF_PI phi) 2)) con)]
    (vec2
     (* EARTH_RADIUS_MAJOR (m/radians lon))
     (* (- EARTH_RADIUS_MAJOR) (Math/log ts)))))

(defn lat-log
  [lat] (Math/log (Math/tan (+ (/ (m/radians lat) 2) m/QUARTER_PI))))

(defn mercator-in-rect
  [[lon lat] [left right top bottom] w h]
  (let [lon              (m/radians lon)
        left             (m/radians left)
        [lat top bottom] (map lat-log [lat top bottom])]
    (vec2
      (* w (/ (- lon left) (- (m/radians right) left)))
      (* h (/ (- lat top) (- bottom top))))))