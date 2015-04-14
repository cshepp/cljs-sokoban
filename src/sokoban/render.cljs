(ns sokoban.render
    (:require [quil.core :as q :include-macros true]
              [cljs.core.async :refer [<! chan]]
              [sokoban.map :as m])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def state       (atom {}))
(def input-chan  (atom (chan)))
(def cell-size   (int 15))

(def characters {:w {:color [150 150 150] :chr \#}
                 :p {:color [0   255 255] :chr \@}
                 :b {:color [255 0   0  ] :chr \X}
                 :g {:color [0   255 0  ] :chr \•}
                 :n {:color [0   0   0  ] :chr ""}})

(defn setup []
    (q/text-font (q/load-font "Courier New") 18)
    (q/background 0)
    (q/frame-rate 10))

(defn center [w h a b gw gh]
  (let [offset-x (- (int (/ w 2)) (int (/ (* gw cell-size) 2)))
        offset-y (- (int (/ h 2)) (int (/ (* gh cell-size) 2)))]
    (case b
      :x (+ offset-x a)
      :y (+ offset-y a))))

(defn grid->world [a b w h]
  (let [z (* a cell-size)]
    (center 800 300 z b w h)))

(defn draw-cell [cell w h]
    (let [{:keys [coords entity]} cell
          {:keys [color chr]} (get characters entity)
          [x y] coords]
        (q/fill (apply q/color color))
        (q/text chr (grid->world x :x w h) (grid->world y :y w h))
        ;;(q/rect (grid->world x :x w h) (grid->world y :y w h) cell-size cell-size)
        ))

(defn draw-entity [e i w h]
  (let [coords (m/index->coords i w)]
    (draw-cell {:coords coords :entity e} w h)))

(defn draw-player [i w h]
  (draw-entity :p i w h))

(defn draw-box [i w h]
  (draw-entity :b i w h))

(defn draw-tile [i kw w h]
  (let [coords (m/index->coords i w)]
    (draw-cell {:coords coords :entity kw} w h)))

(defn draw-screen [{:keys [tiles entities width height]}]
  (doall (map-indexed #(draw-tile %1 %2 width height) tiles))
  (doall (map #(draw-player % width height) (:player entities)))
  (doall (map #(draw-box % width height) (:boxes entities))))

(defn draw-win []
  (q/fill 255)
  (q/text "YOU WIN!" 330 100))

(defn draw []
  (q/background 0)
  (let [s @state
        w (:win s)]
    (if w
      (draw-win)
      (draw-screen s))))

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
