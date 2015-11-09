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

;; the app state will be populated with a :mesh key
;; when the webgl-canvas component (below) initializes
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
  "Defines a WebGL canvas component using reagent.core/create-class,
  which allows us to use the various React lifecycle methods.
  The :component-did-mount fn is only run once to initialize the component
  and here used to do the following:

  - setup all WebGL elements (context, mesh & shader)
  - store the generated mesh instance in the app-state atom
  - attach an update loop/function triggered during every render cycle
    to animate the scene

  The :reagent-render function creates the component's canvas element"
  [id]
  (r/create-class
  {:component-did-mount
   #(let [gl         (gl/gl-context id)
           view-rect (gl/get-viewport-rect gl)
           teeth     10
           ;; define a gear mesh with given number of teeth and profile shape
           ;; the gear is initially a 2d polygon which is then extruded as 3d mesh
           mesh      (g/extrude-shell
                       (poly/cog 0.5 teeth [0.9 1 1 0.9])
                       {:mesh (gmesh) :depth 0.1 :inset 0.025 :wall 0.015 :bottom? true})
           ;; now we can prepare the mesh for WebGL rendering by translating it
           ;; into a number of WebGL data buffers and associate a shader preset
           ;; the shader itself is customizable via the :uniforms listed below
           ;; see https://github.com/thi-ng/geom/blob/master/geom-webgl/src/shaders.org#phong
           model     (-> mesh
                         (gl/as-webgl-buffer-spec {})
                         (buf/make-attribute-buffers-in-spec gl gl/static-draw)
                         (assoc :shader (sh/make-shader-from-spec gl phong/shader-spec))
                         (update-in [:uniforms] merge
                                    {:view        (mat/look-at (vec3 0 0 2) (vec3) v/V3Y)
                                     :proj        (gl/perspective 45 view-rect 0.1 10.0)
                                     :lightPos    (vec3 0.1 0 1)
                                     :ambientCol  0x111111
                                     :diffuseCol  0xff3310
                                     :specularCol 0xcccccc
                                     :shininess   100
                                     :wrap        0
                                     :useBlinnPhong true}))
           ;; record current time stamp (used as reference for animation)
           t0        (.getTime (js/Date.))
           ;; animation fn, triggered at each React render cycle
           update    (fn update []
                       (let [;; compute elapsed time (in seconds)
                             t     (* (- (.getTime (js/Date.)) t0) 0.001)
                             ;; new timebased rotation matrix (base for both gears)
                             ;; M44 is the 4x4 identity matrix
                             rot   (g/rotate-y M44 (* t 1.))
                             ;; matrix (coordinate system) for 1st gear
                             ;; multiplying matrices = transforming coordinate systems
                             tx1   (g/* rot (-> M44
                                                (g/translate -0.46 0 0)
                                                (g/rotate-y 0.3)
                                                (g/rotate-z t)))
                             ;; matrix for 2nd gear
                             tx2   (g/* rot (-> M44
                                                (g/translate 0.46 0 0)
                                                (g/rotate-y -0.3)
                                                (g/rotate-z (- (+ t (/ HALF_PI teeth))))))]
                         ;; clear background & depth buffer
                         (gl/clear-color-buffer gl 1 1.0 1.0 1.0)
                         (gl/clear-depth-buffer gl 1)
                         ;; draw 1st gear
                         (phong/draw
                           gl (assoc-in model [:uniforms :model] tx1))
                         ;; draw 2nd gear with modified transform matrix & color
                         (phong/draw
                          gl (-> model
                                 (assoc-in [:uniforms :model] tx2)
                                 (assoc-in [:uniforms :diffuseCol] 0x2277ff)))
                         ;; retrigger update fn in next render cycle
                         (r/next-tick update)))]
      ;; store mesh in app-state
      (swap! app-state assoc :mesh mesh)
      ;; setup viewport
      (gl/set-viewport gl view-rect)
      (gl/enable gl gl/depth-test)
      ;; kick off update loop
      (r/next-tick update))
   
   ;; :display-name is used for React.js developer tools
   :display-name   id
   
   ;; the render fn merely constructs the canvas
   ;; width & height could be passed in arguments to the parent webgl-canvas fn
   ;; or could be kept in app-state and referenced from there
   ;; the latter is useful when creating a full-window canvas which needs to be resizable
   ;; left as exercise for the reader...
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