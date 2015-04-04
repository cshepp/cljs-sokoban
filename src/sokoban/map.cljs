(ns sokoban.map)

(defn deconstruct-col [col-num row-num cell]
    {:coords [col-num row-num] :entity cell})

(defn deconstruct-row [row-num row]
    (map-indexed #(deconstruct-col %1 row-num %2) row))

(defn deserialize-state [state]
  (reduce into [] (map-indexed deconstruct-row state)))