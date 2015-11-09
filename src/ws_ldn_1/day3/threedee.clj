(ns ws-ldn-1.threedee
  "3D mesh examples & export"
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v]
    [thi.ng.geom.circle :as c]
    [thi.ng.geom.polygon :as poly]
    [thi.ng.geom.sphere :as s]
    [thi.ng.geom.gmesh :as gm]
    [thi.ng.geom.mesh.io :as mio]
    [thi.ng.geom.mesh.subdivision :as sd]
    [thi.ng.geom.mesh.csg :as csg]
    [clojure.java.io :as io]))

(defn save-stl
  "Takes file path and mesh instance, saves mesh as STL."
  [path mesh]
  (with-open [out (io/output-stream path)]
    (mio/write-stl
      (mio/wrapped-output-stream out)
      (g/tessellate mesh))))

(def mesh
  "Generates subdivided, hollow hexagon cylinder mesh from 2D circle"
  (-> (c/circle 10)
      (g/extrude {:depth 20 :res 6 :wall 2 :mesh (gm/gmesh)})
      (sd/catmull-clark)
      (sd/catmull-clark)
      (sd/catmull-clark)))

(def csg-mesh
  "Generates mesh from 3 intersecting spheres, uses CSG module
  to subtract the outer ones from the center sphere."
  (let [s (g/as-mesh (s/sphere 10) {:res 20})
        a (csg/mesh->csg s)
        b (csg/mesh->csg (g/translate s (v/vec3 -10 0 0)))
        c (csg/mesh->csg (g/translate s (v/vec3 10 0 0)))]
    (csg/csg->mesh
      (reduce csg/subtract [a b c]))))

(save-stl "mesh.stl" mesh)
(save-stl "csg-mesh.stl" csg-mesh)
