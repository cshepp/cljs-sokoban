(ns sokoban.core
    (:require [cljs.core.async :as async :refer [<! >! chan]]
              [cljs.core.match :refer-macros [match]]
              [sokoban.render :as r]
              [sokoban.map :as m])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

#_(def level [[:w :w :w :w :w :w :w]
            [:w :p :w :n :n :n :w]
            [:w :n :n :b :n :g :w]
            [:w :w :n :b :g :n :w]
            [:w :n :n :n :n :n :w]
            [:w :w :w :w :w :w :w]])

(def level [[:w :w :w :w :w :w :w :w]
            [:w :n :n :n :w :n :n :w]
            [:w :n :w :n :w :b :g :w]
            [:w :n :n :n :n :b :g :w]
            [:w :n :w :n :w :b :g :w]
            [:w :n :n :n :w :n :n :w]
            [:w :w :w :w :w :p :n :w]
            [:n :n :n :n :w :w :w :w]])

#_(def level [[:n :n :n :n :w :w :w :w :w :n :n :n :n :n :n :n]
            [:n :n :n :n :w :n :n :n :w :n :n :n :n :n :n :n]
            [:n :n :n :n :w :b :n :n :w :n :n :n :n :n :n :n]
            [:n :n :w :w :w :n :n :b :w :w :n :n :n :n :n :n]
            [:n :n :w :n :n :b :n :b :n :w :n :n :n :n :n :n]
            [:w :w :w :n :w :n :w :w :n :w :w :w :w :w :w :w]
            [:w :n :n :n :w :n :w :w :n :w :w :n :n :g :g :w]
            [:w :n :b :n :n :b :n :n :n :n :p :n :n :g :g :w]
            [:w :w :w :w :w :n :w :w :w :n :w :n :n :g :g :w]
            [:n :n :n :n :w :n :n :n :n :n :w :w :w :w :w :w]
            [:n :n :n :n :w :w :w :w :w :w :w :n :n :n :n :n]])

#_(def level 
  (vec (take 19
    (repeat (vec (take 70 (repeat :w)))
  ))))

(def state (atom {}))

(defn has-entity [i]
  (>= i 0))

(defn get-two [f i w]
  [(f i w) (f (f i w) w)])

(defn key->func [k]
  (case k
    :w m/above
    :a m/left
    :s m/below
    :d m/right
    (fn [x] x)))

(defn key->neighbors [k {:keys [tiles width entities]}]
  (let [i (first (:player entities))
        e (:boxes entities)
        f (key->func k)
        n (get-two f i width)
        t (mapv #(m/get-at % tiles width) n)
        b (mapv (fn [g] (.indexOf (clj->js e) (clj->js g))) n)]
    (vec (map-indexed #(if (has-entity %2)
                             :b
                             (nth t %1))
                      b))))

(defn move-box [k state]
  (let [{:keys [tiles width entities]} state
        p (first (:player entities))
        e (:boxes entities)
        f (key->func k)
        n (first (get-two f p width))
        i (.indexOf (clj->js e) (clj->js n))]
    (update-in state [:entities :boxes i] f width)))

(defn move-player [k state]
  (let [{:keys [entities width]} state
        p (first (:player entities))
        f (key->func k)]
    (update-in state [:entities :player 0] f width)))

(defn update [k state]
  (if-not (nil? (key->neighbors k state))
    (let [n (key->neighbors k state)
          s (match [n]
              [[:n  _]] (move-player k state)
              [[:b :n]] (move-player k (move-box k state))
              [[:b :g]] (move-player k (move-box k state))
              [[:g  _]] (move-player k state)
              [[ _  _]] state)]
      s)))

(defn win? [a b]
  (= (sort a) (sort b)))

(defn check-win [state]
  (let [{:keys [entities tiles]} state
        b (:boxes entities)
        g (vec (keep-indexed #(if (= :g %2) %1) tiles))
        w (win? b g)]
    (assoc state :win w)))

(defn log [x]
  (println x)
  x)

(defn on-input [input-chan render-chan]
  (go-loop []
    (let [v (<! input-chan)]
      (go
        (if (= v :r)
          (start-level level))
        (->> @state
          (update v)
          (check-win)
          (log)
          (reset! state)
          (>! render-chan))))
    (recur)))

;; start a given level (can call more than once)
(defn start-level [level]
  (reset! state (m/load-level level)))

;; initialize game (call once!)
(defn start-game [level]
  (let [render-chan (chan)
        input-chan  (chan)]
    (r/update render-chan)
    (on-input input-chan render-chan)
    (r/init input-chan)
    (start-level level)
    (go
      (>! render-chan @state))))

(start-game level)