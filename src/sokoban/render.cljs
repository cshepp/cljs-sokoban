(ns sokoban.render
    (:require [quil.core :as q :include-macros true]
              [cljs.core.async :refer [<! chan]]
              [sokoban.map :as m])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def state       (atom {}))
(def input-chan  (atom (chan)))
(def cell-height (int  15))
(def cell-width  (int  11))

(def characters {:w {:color [150]     :chr \#}
                 :p {:color [255 0 0] :chr \@}
                 :b {:color [200]     :chr \$}
                 :g {:color [0 255 0] :chr \•}
                 :n {:color [50]      :chr ""}})

(defn setup []
    (q/text-font (q/load-font "Courier New") 18)
    (q/background 0)
    (q/frame-rate 10))

(defn draw-cell [cell]
    (let [{:keys [coords entity]} cell
          {:keys [color chr]} (get characters entity)
          [x y] coords]
        (q/fill (apply q/color color))
        (q/text chr (* (inc x) cell-width) (* (inc y) cell-height))))

(defn draw-entity [e i w]
  (let [coords (m/index->coords i w)]
    (draw-cell {:coords coords :entity e})))

(defn draw-player [i w]
  (draw-entity :p i w))

(defn draw-box [i w]
  (draw-entity :b i w))

(defn draw-tile [i kw w]
  (let [coords (m/index->coords i w)]
    (draw-cell {:coords coords :entity kw})))

(defn draw-screen [{:keys [tiles entities width]}]
  (doall (map-indexed #(draw-tile %1 %2 width) tiles))
  (doall (map #(draw-player % width) (:player entities)))
  (doall (map #(draw-box % width) (:boxes entities))))

(defn draw-win []
  (q/fill 255)
  (q/text "YOU WIN!" 20 20))

(defn draw []
  (q/background 0)
  (let [s @state
        w (:win s)]
    (if w
      (draw-win)
      (draw-screen s))))

(defn on-key-typed []
  (let [k (q/key-as-keyword)]
    (println k)
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
