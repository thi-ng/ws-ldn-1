(defproject thi.ng/ws-ldn-1 "0.1.0-SNAPSHOT"
  :description   "thi.ng workshop #1"
  :url           "http://thi.ng"
  :license       {:name "Apache Software License 2.0"
                  :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies  [[org.clojure/clojure "1.7.0"]
                  [org.clojure/clojurescript "1.7.170"]
                  [org.clojure/data.csv "0.1.3"]
                  [reagent "0.5.1"]
                  [thi.ng/geom "0.0.908"]
                  [thi.ng/strf "0.2.1"]]
  
  :plugins       [[lein-figwheel "0.5.0-SNAPSHOT"]
                  [lein-cljsbuild "1.1.1"]]
  
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild     {:builds [{:id "day2"
                            :source-paths ["src-cljs-day2"]
                            :figwheel true
                            :compiler {:main "ws-ldn-1.ui.day2.core"
                                       :asset-path "js/day2"
                                       ;;:optimizations :advanced
                                       ;;:pretty-print false
                                       :output-to "resources/public/js/app.js"
                                       :output-dir "resources/public/js/day2"}}
                           {:id "day3-viz"
                            :source-paths ["src-cljs-day3-1"]
                            :figwheel true
                            :compiler {:main "ws-ldn-1.ui.day3.viz"
                                       :asset-path "js/day3-viz"
                                       ;;:optimizations :advanced
                                       ;;:pretty-print false
                                       :output-to "resources/public/js/app.js"
                                       :output-dir "resources/public/js/day3-viz"}}
                           {:id "day3-webgl"
                            :source-paths ["src-cljs-day3-2"]
                            :figwheel true
                            :compiler {:main "ws-ldn-1.ui.day3.webgl"
                                       :asset-path "js/day3-webgl"
                                       ;;:optimizations :advanced
                                       ;;:pretty-print false
                                       :output-to "resources/public/js/app.js"
                                       :output-dir "resources/public/js/day3-webgl"}}]})