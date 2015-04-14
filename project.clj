(defproject sokoban "0.1.0-SNAPSHOT"
    :dependencies [[org.clojure/clojure "1.6.0"]
                   [org.clojure/clojurescript "0.0-3126"]
                   [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                   [org.clojure/core.match "0.3.0-alpha4"]
                   [quil "2.2.5"]]
    :plugins [[lein-cljsbuild "1.0.5"]
              [lein-kibit "0.0.8"]]
    :cljsbuild {:builds
                {:min {:source-paths ["src"]
                       :compiler {:output-to "out/main.js"}}}})