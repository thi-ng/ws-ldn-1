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

(defn mercator-normalized
  ([lon lat] (mercator-normalized lon lat 1.0))
  ([lon lat h]
   (let [nx    2.0037508342789244E7 ;; mercator lon @ +/-180 deg
         ny    3.464067325399331E7  ;; mercator lat @ +/-90 deg
         w     (* h (/ nx ny))
         [x y] (mercator lat lon)]
     (vec2 (* (/ x nx) w) (* (/ y ny) h)))))
