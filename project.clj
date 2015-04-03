(defproject sokoban "0.1.0-SNAPSHOT"
    :dependencies [[org.clojure/clojure "1.6.0"]
                   [org.clojure/clojurescript "0.0-3126"]
                   [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                   [quil "2.2.5"]]
    :plugins [[lein-cljsbuild "1.0.5"]]
    :cljsbuild {:builds
                {:min {:source-paths ["src"]
                       :compiler {:output-to "out/main.js"}}}})