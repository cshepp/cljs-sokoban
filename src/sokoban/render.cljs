(ns sokoban.render
    (:require [quil.core :as q :include-macros true]
              [cljs.core.async :refer [<! chan]])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def state       (atom []))
(def input-chan  (atom (chan)))
(def cell-height (int  18))
(def cell-width  (int  18))

(def characters {:w {:color 150 :chr \#}
                 :p {:color 255 :chr \@}
                 :b {:color 200 :chr \$}
                 :g {:color 200 :chr \.}
                 :n {}})

(defn setup []
    (q/text-font (q/load-font "Courier New") 18)
    (q/background 0)
    (q/frame-rate 30))

(defn draw-cell [x y cell]
    (let [{:keys [color chr]} (get characters cell)]
        (q/fill color)
        (q/text chr (* (inc x) cell-width) (* (inc y) cell-height))))

(defn draw-row [y row]
    (doall (map-indexed #(draw-cell %1 y %2) row)))

(defn draw-screen [state]
    (doall (map-indexed #(draw-row %1 %2) state)))

(defn draw []
    (q/background 0)
    (draw-screen @state))

(defn on-key-typed []
  (let [k (q/key-as-keyword)]
      (go
        (>! @input-chan k))))

(defn init [ch]
  (reset! input-chan ch)
  (q/sketch
    :host "canvas-id"
    :setup setup
    :draw draw
    :key-typed on-key-typed
    :size [800 300]))


(defn update [ch]
    (go-loop []
      (let [s (<! ch)]
        (reset! state s))
      (recur)))
