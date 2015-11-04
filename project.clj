(defproject thi.ng/ws-ldn-1 "0.1.0-SNAPSHOT"
  :description  "thi.ng workshop #1"
  :url          "http://thi.ng"
  :license      {:name "Apache Software License 2.0"
                 :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/data.csv "0.1.3"]
                 [reagent "0.5.1"]
                 [thi.ng/geom "0.0.881"]
                 [thi.ng/strf "0.2.1"]]
  
  :plugins      [[lein-figwheel "0.4.1"]]
  
  :cljsbuild {:builds [{:id "day2" 
                        :source-paths ["src-cljs"]
                        :figwheel true
                        :compiler {:main "ws-ldn-1.ui.day2.core"
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"}}
                       {:id "day3" 
                        :source-paths ["src-cljs"]
                        :figwheel true
                        :compiler {:main "ws-ldn-1.ui.day3.core"
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"}}]
})