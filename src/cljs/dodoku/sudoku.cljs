(ns dodoku.sudoku
  (:require [clojure.set :as set]))

(def all-values (set (range 1 10)))

(defn value
  ([b rc]
   (value b (first rc) (second rc)))
  ([b r c]
   (nth (nth b r) c)))

(defn row [b r]
  (set (nth b r)))

(defn column [board i]
  (set (map nth board (repeat i))))

(defn square-index [r c]
  (+ (quot c 3) (* 3 (quot r 3))))


(defn- agg-by-val [m k v]
  (let [new-k (apply square-index k)]
    (update m new-k conj v)))

(def indices (into [] (for [r (range 9)
                            c (range 9)]
                        [r c])))

(defn values [board]
  (map value (repeat board) indices))

(reduce-kv identity {} {})

(defn squares [board]
  (let [indices-groups (zipmap indices (values board))
        kvs (reduce-kv agg-by-val {} indices-groups)]
    (into {} (for [[k v] kvs]
               [k (set v)]))))

(defn square-by-idx [b i]
  (get (squares b) i))

(defn square [b r c]
  (get (squares b) (square-index r c)))

(defn used
  ([b rc]
   (used b (first rc) (second rc)))
  ([b r c]
   (set (concat
         (row b r)
         (column b c)
         (square b r c)))))

(defn candidates
  ([b r c]
   (let [v (value b r c)]
     (if (nil? v)
       (set/difference all-values (used b r c))
       #{v})))
  ([b rc]
   (candidates b (first rc) (second rc))))

(defn solved? [b]
  (let [rows (mapcat (partial row b) (range 9))
        columns (mapcat (partial column b) (range 9))
        squares (mapcat (partial square-by-idx b) (range 9))]
    (= all-values
       (set (concat rows columns squares)))))

(defn set-value
  ([b r c v]
   (let [new-row (assoc (nth b r) c v)]
     (assoc (vec b) r new-row)))
  ([b rc v]
   (set-value b (first rc) (second rc) v)))

(defn colliding? [board index]
  (let [board-without-self (set-value board index nil)
        cands (candidates board-without-self index)
        val (value board index)]
    (and (not (nil? val))
         (not (cands val)))))

(defn collisions [board]
  (set (filter (partial colliding? board) indices)))

(defn solve
  ([board _indices]
   (if (empty? _indices)
     board
     (let [index (first _indices)]
       (let [candidates (candidates board index)]
         (loop [removed #{}]
           (when-let [valid-candidates (set/difference candidates removed)]
             (when (not (empty? valid-candidates))
               (let [candidate (first valid-candidates)
                     next-board (set-value board index candidate)
                     solution (solve next-board (rest _indices))]
                 (if solution
                   solution
                   (recur (conj removed candidate)))))))))))
  ([board]
   (if (empty? (collisions board))
     (solve board indices)
     (do (js/alert "Can not solve board. Please test for collisions.")
         board))))
