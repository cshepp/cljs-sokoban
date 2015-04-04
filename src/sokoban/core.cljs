(ns sokoban.core
    (:require [quil.core :as q :include-macros true]
              [cljs.core.async :as async :refer [<! >! chan]]
              [sokoban.render :as r]
              [sokoban.map :as m])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def serialized-state (atom [[:w :w :w :w :w :w :w]
                             [:w :n :n :n :n :n :w]
                             [:w :n :p :b :g :n :w]
                             [:w :n :n :n :n :n :w]
                             [:w :w :w :w :w :w :w]]))

(defn on-input [ch]
  (go-loop []
    (let [v (<! ch)]
      (do
        (println (str "key " (prn-str v)))))
    (recur)))

(let [render-chan (chan)
      input-chan  (chan)]
    (r/update render-chan)
    (on-input input-chan)
    (r/init input-chan)
    (go
        (>! render-chan @deserialized-state)))