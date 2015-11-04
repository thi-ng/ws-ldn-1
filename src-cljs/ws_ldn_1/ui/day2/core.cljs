(ns ^:figwheel-always ws-ldn-1.ui.day2.core
  (:require
    [reagent.core :as r]))

(enable-console-print!)

(defonce app-state (r/atom {:history []}))

(def error-state (r/atom nil))

(defn set-error!
  [err]
  (reset! error-state err)
  (when err
    (js/setTimeout #(set-error! nil) 1000)))

(defn swap*!
  "Similar to clojure.core/swap!, but records history and returns atom."
  [ref f & args]
  (swap! ref
         (fn [ref-val] (apply f (update ref-val :history conj ref-val) args)))
  ref)

(defn undo!
  "Restores atom to previous history state (unless there's isn't any)."
  [ref]
  (let [history (:history @ref)]
    (if (< 1 (count history))
      (reset! ref (last history))
      (set-error! "can't undo further..."))))

(defn checkbox
  "Checkbox component wired to :checks key in app-state (see init-app fn below)"
  [id {:keys [checked col]}]
  [:div {:key (str "check" id) :style {:background col}}
   [:input
    {:type      "checkbox"
     :checked   checked
     :on-change #(swap*! app-state update-in [:checks id :checked] not)}]
   col])

(defn user-error
  "Component displaying current error message"
  []
  (when-let [err @error-state]
    [:p {:style {:background "red" :padding "10px" :color "white"}} err]))

(defn main-panel
  "Application root component"
  []
  [:div
   [user-error]
   [:p "Current counter: " (:counter @app-state)]
   (map-indexed checkbox (:checks @app-state))
   [:p "App state:"]
   [:textarea {:cols 60 :rows 5 :value (pr-str @app-state)}]
   [:p
    [:button {:on-click #(undo! app-state)} "Undo"]
    [:button {:on-click #(swap*! app-state update :counter inc)} "Next"]]])

(defn init-app
  "Initializes app-state atom with default state"
  []
  (swap*! app-state merge
          {:counter 0
           :checks  [{:checked false :col "yellow"}
                     {:checked true :col "cyan"}
                     {:checked false :col "red"}]}))

(defn main
  "Application main entry point. Initializes app-state and
  kicks off React component lifecycle."
  []
  (init-app)
  (r/render-component
    [main-panel]
    (.-body js/document)
    #_(.getElementById js/document "main-area")))

(comment
  ;; basic JS interop & DOM manipulation example
  (defn main
    []
    (let [el (.getElementById js/document "main-area")]
      (set! (.-innerHTML el) "<h1>Coffee time</h1>"))))

;; (main)