(ns dodoku.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [clerk.core :as clerk]
   [dodoku.sudoku :as sudoku]
   [accountant.core :as accountant]
   [clojure.string :as str]))


(def board (atom (vec (repeat 9 (vec (repeat 9 nil))))))
(def collisions (atom #{}))

(defn update-board [b row column value]
  (let [value (if (= value "")
                nil
                (int value))
        new-row (assoc (nth b row) column value)
        new-b (assoc b row new-row)]
    new-b))

(defn square-border [row column]
  (let [top (zero? (rem row 3))
        left (zero? (rem column 3))
        bottom (zero? (rem (inc row) 3))
        right (zero? (rem (inc column) 3))
        border-str #(if % "3px solid black" "0px solid black")]
    {:border-top (border-str top)
     :border-right  (border-str right)
     :border-bottom  (border-str bottom)
     :border-left (border-str left)}))

(defn board-cell [row column]
  (fn []
    [:td
     [:select
      {:on-change #(swap! board update-board row column (-> % .-target .-value))
       :value (if-let [v (get-in @board [row column])]
                v
                "X")
       :style (merge (square-border row column)
                     {:-webkit-appearance "none"
                      :-moz-appearance "none"
                      :appearance "none"
                      :background-color (if (@collisions [row column])
                                          "red"
                                          "white")})}
      [:option
       {:key ""
        :value ""} "X"]
      (for [v (range 1 10)]
        [:option
         {:key v
          :value v}
         v])]]))

(defn board-row [row]
  (fn []
    [:tr
     (for [column (range 9)]
       ^{:key [row column]} [board-cell row column])]))

(defn board-component []
  (fn []
    [:div
     {:align "center"}
     [:button
      {:on-click #(reset! collisions (sudoku/collisions @board))}
      "test collisions"]
     [:button
      {:on-click #(swap! board sudoku/solve)}
      "solve"]
     [:table
      [:tbody
       {:style {:border-collapse "collapse"}}
       (for [i (range 9)]
         ^{:key i} [board-row i])]]]))

(defn home-page []
  (fn []
    (board-component)))


;; -------------------------
;; Initialize app

;;(defn mount-root []
;;(reagent/render [current-page] (.getElementById js/document "app")))


(defn init! []
  (reagent/render [home-page] (.getElementById js/document "app")))
