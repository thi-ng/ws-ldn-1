(ns ws-ldn-1.ui.day3.webgl
  "WebGL reagent example"
  (:require
    [reagent.core :as r]
    [thi.ng.geom.webgl.core :as gl]
    [thi.ng.geom.webgl.animator :as anim]
    [thi.ng.geom.webgl.buffers :as buf]
    [thi.ng.geom.webgl.shaders :as sh]
    [thi.ng.geom.webgl.utils :as glu]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2 vec3]]
    [thi.ng.geom.core.matrix :as mat :refer [M44]]
    [thi.ng.geom.webgl.shaders.phong :as phong]
    [thi.ng.geom.circle :as c]
    [thi.ng.geom.polygon :as poly]
    [thi.ng.geom.basicmesh :refer [basic-mesh]]
    [thi.ng.geom.gmesh :refer [gmesh]]
    [thi.ng.geom.mesh.io :as mio]
    [thi.ng.geom.mesh.subdivision :as sd]
    [thi.ng.typedarrays.core :as arrays]
    [thi.ng.math.core :as m :refer [PI HALF_PI TWO_PI]]
    [thi.ng.dstruct.streams :as streams]))

(defonce app-state (r/atom {}))

(defn save-mesh
  "Triggers download of the mesh (in STL format) stored under the :mesh key
  in the app-state atom to the user's drive."
  []
  (let [out (mio/wrapped-output-stream (streams/output-stream))]
    (mio/write-stl out (g/tessellate (:mesh @app-state)))
    (let [url (streams/as-data-url out)]
      (js/setTimeout (fn [] (set! (.-href js/location) @url)) 500))))

(defn webgl-canvas
  "Defines a WebGL canvas component using reagent/create-class which allows us to
  use the various React lifecycle methods.
  The :component-did-mount fn is only run once to initialize the component and here
  used to do the following:

  - setup all WebGL elements (context, mesh & shader)
  - store the generated mesh instance in the app-state atom
  - attach an update loop/function triggered during every render cycle to animate the scene

  The :reagent-render function creates the component's canvas element"
  [id]
  (r/create-class
  {:component-did-mount
   #(let [gl         (gl/gl-context id)
           view-rect (gl/get-viewport-rect gl)
           teeth     10
           mesh      (g/extrude-shell
                       (poly/cog 0.5 teeth [0.9 1 1 0.9])
                       {:mesh (gmesh) :depth 0.1 :inset 0.025 :wall 0.015 :bottom? true})
           model     (-> mesh
                         (gl/as-webgl-buffer-spec {})
                         (buf/make-attribute-buffers-in-spec gl gl/static-draw)
                         (assoc :shader (sh/make-shader-from-spec gl phong/shader-spec))
                         (update-in [:uniforms] merge
                                    {:proj        (gl/perspective 45 view-rect 0.1 10.0)
                                     :lightPos    (vec3 0.1 0 1)
                                     :ambientCol  0x111111
                                     :diffuseCol  0xff3310
                                     :specularCol 0xcccccc
                                     :shininess   100
                                     :wrap        0
                                     :useBlinnPhong true}))
           t0        (.getTime (js/Date.))
           update    (fn update []
                       (let [t     (* (- (.getTime (js/Date.)) t0) 0.001)
                             model (assoc-in
                                     model [:uniforms :view]
                                     (mat/look-at (vec3 0 0 2) (vec3) v/V3Y))
                             rot   (g/rotate-y M44 (* t 1.))
                             tx1   (g/* rot (-> M44
                                                (g/translate -0.46 0 0)
                                                (g/rotate-y 0.3)
                                                (g/rotate-z t)))
                             tx2   (g/* rot (-> M44
                                                (g/translate 0.46 0 0)
                                                (g/rotate-y -0.3)
                                                (g/rotate-z (- (+ t (/ HALF_PI teeth))))))]
                         (gl/set-viewport gl (g/scale view-rect 1))
                         (gl/clear-color-buffer gl 1 1.0 1.0 1.0)
                         (gl/clear-depth-buffer gl 1)
                         (gl/enable gl gl/depth-test)
                         (phong/draw gl (assoc-in model [:uniforms :model] tx1))
                         (phong/draw
                          gl (-> model
                                 (assoc-in [:uniforms :model] tx2)
                                 (assoc-in [:uniforms :diffuseCol] 0x2277ff)))
                         (r/next-tick update)))]
      (swap! app-state assoc :mesh mesh)
      (r/next-tick update))
   :display-name   id
   :reagent-render (fn [] [:canvas {:key id :id id :width 640 :height 480}])}))

(defn app-component
  "Main React root/application component"
  []
  [:div
   [webgl-canvas "main"]
   [:div
    [:p "Download the gear 3d model as STL (the downloaded file
    should be renamed with the .stl file extension -
    can't be specified via JS)"]
    [:p "Use " [:a {:href "http://meshlab.sf.net"} "Meshlab"] " to view the file."]
    [:p [:button {:on-click save-mesh} "Download STL"]]]])

(defn main
  "App entry point"
  []
  (r/render-component [app-component] (.-body js/document)))

(main)