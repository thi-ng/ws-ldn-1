(ns ws-ldn-1.ui.day3.webgl
  "WebGL reagent example: Interactive gear mesh generator & animation
  Demonstrates usage of React component lifecycle stages and how to
  dynamically update parts of the WebGL scene via UI controls."
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
    [thi.ng.dstruct.streams :as streams]
    [thi.ng.strf.core :as f]))

;; the app state will be updated when the webgl-canvas component (below) initializes
(defonce app-state (r/atom {:solid? true :depth 0.1 :teeth 10 :inner 0.8}))

(defn save-mesh
  "Triggers download of the mesh (in STL format) stored under the :mesh key
  in the app-state atom to the user's drive."
  []
  (let [out (mio/wrapped-output-stream (streams/output-stream))]
    (mio/write-stl out (g/tessellate (:mesh @app-state)))
    (let [url (streams/as-data-url out)]
      (js/setTimeout (fn [] (set! (.-href js/location) @url)) 500))))

(defn generate-mesh
  "Defines a gear mesh with given number of teeth, inner radius for
  profile shape and extrusion depth. The gear is initially a 2d polygon
  which is then extruded as 3d mesh. Updates :solid?, :teeth, :inner,
  :depth, :mesh and :model keys in app-state atom.
  Returns WebGL model structure (a map)."
  [gl solid? teeth inner depth]
  (let [poly  (poly/cog 0.5 teeth [inner 1 1 inner])
        mesh  (if solid?
                (g/extrude
                  poly {:mesh (gmesh) :depth depth :scale (- 1 depth)})
                (g/extrude-shell
                  poly {:mesh (gmesh) :depth depth :inset 0.025 :wall 0.015 :bottom? true}))
        model (-> mesh
                  (gl/as-webgl-buffer-spec {})
                  (buf/make-attribute-buffers-in-spec gl gl/static-draw))]
    (swap! app-state
           (fn [state]
             (-> state
                 (assoc :solid? solid? :teeth teeth :inner inner :depth depth)
                 (assoc :mesh mesh)
                 (update :model merge model))))
    model))

(defn checkbox
  "HTML checkbox with label"
  [opts label]
  [:div [:input (assoc opts :type "checkbox") label]])

(defn slider
  "HTML5 slider component"
  [opts label]
  [:div
   [:input (assoc opts :type "range")] " "
   (or (:defaultValue opts) (:value opts)) " "
   label])

(defn event-value
  "Helper fn to retrieve an event's target DOM element value attrib"
  [e] (-> e .-target .-value))

(defn webgl-canvas
  "Defines a WebGL canvas component using reagent.core/create-class,
  which allows us to use the various React lifecycle methods.
  The :component-did-mount fn is only run once to initialize the component
  and here used to do the following:

  - setup all WebGL elements (context, mesh & shader)
  - store the generated mesh instance & webgl model structure in the app-state atom
  - attach an update loop/function triggered during every render cycle
    to animate the scene

  The :reagent-render function creates the component's canvas element and various
  UI controls (sub-components) to allow user to adjust mesh parameters."
  [id]
  (r/create-class
    {:component-did-mount
     #(let [gl        (gl/gl-context id)
            view-rect (gl/get-viewport-rect gl)
            ;; record current time stamp (used as reference for animation)
            t0        (.getTime (js/Date.))
            ;; animation fn, triggered at each React render cycle
            update    (fn update []
                        (let [;; extract keys from app state
                              {:keys [solid? teeth model inner depth]} @app-state
                              ;; compute elapsed time (in seconds)
                              t      (* (- (.getTime (js/Date.)) t0) 0.001)
                              ;; new timebased rotation matrix (base for both gears)
                              ;; M44 is the 4x4 identity matrix
                              rot    (g/rotate-y M44 (* t 1.))
                              ;; matrix (coordinate system) for 1st gear
                              ;; multiplying matrices = transforming coordinate systems
                              offset (+ (/ (inc inner) 4) (if solid? (* teeth 0.0002) (* depth 0.2)))
                              tx1    (g/* rot (-> M44
                                                  (g/translate (- offset) 0 0)
                                                  (g/rotate-y 0.3)
                                                  (g/rotate-z t)))
                              ;; matrix for 2nd gear
                              tx2    (g/* rot (-> M44
                                                  (g/translate offset 0 0)
                                                  (g/rotate-y -0.3)
                                                  (g/rotate-z (- (+ t (/ HALF_PI teeth))))))]
                          ;; clear background & depth buffer
                          (gl/clear-color-buffer gl 1.0 1.0 1.0 1.0)
                          (gl/clear-depth-buffer gl 1.0)
                          ;; draw 1st gear
                          (phong/draw
                            gl (assoc-in model [:uniforms :model] tx1))
                          ;; draw 2nd gear with modified transform matrix & color
                          (phong/draw
                            gl (-> model
                                   (assoc-in [:uniforms :model] tx2)
                                   (assoc-in [:uniforms :diffuseCol] 0x2277ff)))
                          ;; retrigger update fn in next render cycle
                          (r/next-tick update)))
            {:keys [solid? depth teeth inner]} @app-state]
        ;; setup complete WebGL data structure for gear mesh
        (swap! app-state assoc :model
               (-> (generate-mesh gl solid? teeth inner depth)
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
                               :useBlinnPhong true})))
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
     :reagent-render
     (fn []
       (let [{:keys [solid? teeth inner depth]} @app-state
             gl (gl/gl-context id)]
         [:div
          [:canvas {:key id :id id :width 640 :height 480}]
          [checkbox
            {:checked solid?
             :on-change #(generate-mesh gl (-> % .-target .-checked) teeth inner depth)}
            "solid mesh"]
          [slider
           {:min 4 :max 20 :step 2 :defaultValue teeth
            :on-change #(generate-mesh gl solid? (f/parse-int (event-value %) 10) inner depth)}
           "teeth"]
          [slider
           {:min 0.6 :max 0.9 :step 0.05 :defaultValue inner
            :on-change #(generate-mesh gl solid? teeth (f/parse-float (event-value %)) depth)}
           "inner radius"]
          [slider
           {:min 0.1 :max 0.5 :step 0.01 :defaultValue depth
            :on-change #(generate-mesh gl solid? teeth inner (f/parse-float (event-value %)))}
           "extrusion"]]))}))

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