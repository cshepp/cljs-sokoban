(ns sokoban.map
    (:require [cljs.core.match :refer-macros [match]]))

(defn above [i w]
  (- i w))

(defn below [i w]
  (+ i w))

(defn left  [i w]
  (- i 1))

(defn right [i w]
  (+ i 1))

(defn get-at
  ([x y coll w] (get-at (coords->index x y w) coll w))
  ([i coll w]   (if (and (<= i (count coll)) (>= i 0))
                      (nth coll i))))

(defn index->coords [a w]
  (let [x (mod a w)
        y (int (/ a w))]
      [x y]))

(defn coords->index [x y w]
  (+ (* y w) x))

(defn load-entities [level]
  {:player (vec (keep-indexed #(if (= %2 :p) %1) (flatten level)))
   :boxes  (vec (keep-indexed #(if (= %2 :b) %1) (flatten level)))})

(defn load-tiles [level]
  (mapv (fn [x] (case x
                  :p :n
                  :b :n
                  x))
        (flatten level)))

(defn load-level [level]
  {:tiles (load-tiles level)
   :entities (load-entities level)
   :width (count (first level))
   :height (count level)
   :win false})