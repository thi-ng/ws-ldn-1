(ns ^:figwheel-always ws-ldn-1.ui.day3.viz
  "thi.ng/geom visualization demos using reagent."
  (:require
    [reagent.core :as r]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.utils :as gu]
    [thi.ng.geom.svg.core :as svg]
    [thi.ng.geom.svg.adapter :as svgadapt]
    [thi.ng.geom.viz.core :as viz]
    [thi.ng.color.gradients :as grad]
    [thi.ng.math.core :as m :refer [PI]]
    [thi.ng.math.simplexnoise :as n]))

;; 
;; scatter plot

(def scatter-spec
  {:x-axis (viz/log-axis
            {:domain [1 201]
             :range  [50 590]
             :pos    550})
   :y-axis (viz/linear-axis
            {:domain      [0.1 100]
             :range       [550 20]
             :major       10
             :minor       5
             :pos         50
             :label-dist  15
             :label-style {:text-anchor "end"}})
   :grid   {:attribs {:stroke "#caa"}
            :minor-x true
            :minor-y true}
   :data   [{:values  (map (juxt identity #(Math/sqrt %)) (range 0 200 2))
             :attribs {:fill "#0af" :stroke "none"}
             :layout  viz/svg-scatter-plot}
            {:values  (map (juxt identity #(m/random %)) (range 0 200 2))
             :attribs {:fill "none" :stroke "#f60"}
             :shape   (viz/svg-triangle-down 6)
             :layout  viz/svg-scatter-plot}]})

;; line & area plot

(defn test-equation
  [t]
  (let [x (m/mix (- PI) PI t)]
    [x (* (Math/cos (* 0.5 x)) (Math/sin (* x x x)))]))

(def line-spec
  {:x-axis (viz/linear-axis
            {:domain [(- PI) PI]
             :range  [50 580]
             :major  (/ PI 2)
             :minor  (/ PI 4)
             :pos    250})
   :y-axis (viz/linear-axis
            {:domain      [-1 1]
             :range       [250 20]
             :major       0.2
             :minor       0.1
             :pos         50
             :label-dist  15
             :label-style {:text-anchor "end"}})
   :grid   {:attribs {:stroke "#caa"}
            :minor-y true}
   :data   [{:values  (map test-equation (m/norm-range 200))
             :attribs {:fill "none" :stroke "#0af"}
             :layout  viz/svg-line-plot}]})

;; area graph (based on line graph spec)

(def area-spec
  (update-in line-spec [:data 0] merge
             {:attribs {:fill "#0af"}
              :layout viz/svg-area-plot}))

;; bar graph

(defn bar-data-spec
  [num width]
  (fn [idx col]
    {:values     (map (fn [i] [i (m/random 100)]) (range 2000 2016))
     :attribs    {:stroke col :stroke-width (str (dec width) "px")}
     :layout     viz/svg-bar-plot
     :interleave num
     :bar-width  width
     :offset     idx}))

(def bargraph-spec
  {:x-axis (viz/linear-axis
            {:domain [1999 2016]
             :range  [50 580]
             :major  1
             :pos    280
             :label  (viz/default-svg-label int)})
   :y-axis (viz/linear-axis
            {:domain      [0 100]
             :range       [280 20]
             :major       10
             :minor       5
             :pos         50
             :label-dist  15
             :label-style {:text-anchor "end"}})
   :grid   {:minor-y true}
   :data   (map-indexed (bar-data-spec 3 6) ["#0af" "#fa0" "#f0a"])})

;; heatmap

(def hm-matrix
  (->> (for [y (range 10)
             x (range 50)]
         (n/noise2 (* x 0.1) (* y 0.25)))
       (viz/matrix-2d 50 10)))

(defn heatmap-spec*
  [grad-id]
  {:matrix        hm-matrix
   :value-domain  (viz/value-domain-bounds hm-matrix)
   :palette       (->> grad-id
                       (grad/cosine-schemes)
                       (apply grad/cosine-gradient 5120))
   :palette-scale viz/linear-scale
   :layout        viz/svg-heatmap})

(def heatmap-cartesian-spec
  {:x-axis (viz/linear-axis
             {:domain [0 50]
              :range  [50 550]
              :major  10
              :minor  5
              :pos    280})
   :y-axis (viz/linear-axis
             {:domain      [0 10]
              :range       [280 20]
              :major       1
              :pos         50
              :label-dist  15
              :label-style {:text-anchor "end"}})
   :data   [(heatmap-spec* :orange-blue)]})

;; App state handling

(defonce app-state
  (r/atom {:viz-id :bars}))

(def viz-modes
  "Lookup table for the available visualizations"
  {:scatter {:label "Scatter plot" :spec scatter-spec}
   :line    {:label "Line plot" :spec line-spec}
   :area    {:label "Area plot" :spec area-spec}
   :bars    {:label "Bar graph" :spec bargraph-spec}
   :hm      {:label "Heatmap" :spec heatmap-cartesian-spec}})

(defn set-viz-mode!
  "Event handler to update selected visualization mode"
  [e] (swap! app-state assoc :viz-id (-> e .-target .-value keyword)))

;; react components

(defn dropdown
  "Dropdown component. Takes currently selected value, on-change handler
  and a map of menu items, where keys are used as the <option> items' values.
  The map's values are expected to be maps themselves and need to have at
  least a :label key. If the :label is missing the item's key is used as label."
  [sel on-change opts]
  [:select {:defaultValue sel :on-change on-change}
   (map
     (fn [[id val]]
       [:option {:key (str "dd" id) :value (name id)} (or (:label val) (name id))])
     opts)])

(defn visualization
  "Takes a geom.viz visualization spec map and generates SVG component.
  The call to inject-element-attribs ensures that all SVG elements have
  an unique :key attribute, required for React.js."
  [spec]
  (->> spec
       (viz/svg-plot2d-cartesian)
       (svgadapt/inject-element-attribs svgadapt/key-attrib-injector)
       (svg/svg {:width 600 :height 600})))

(defn main-panel
  "Application main component."
  [id]
  (let [viz-id (:viz-id @app-state)] 
    [:div
     [:div "Select visualization: " [dropdown viz-id set-viz-mode! viz-modes]]
     [:div [visualization (get-in viz-modes [viz-id :spec])]]]))

(defn main
  "Application main entry point, kicks off React component lifecycle."
  []
  (r/render-component [main-panel] (.-body js/document)))

(main)