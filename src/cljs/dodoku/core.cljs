(ns dodoku.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [clerk.core :as clerk]
   [dodoku.sudoku :as sudoku]
   [accountant.core :as accountant]))


(def board (atom (vec (repeat 9 (vec (repeat 9 nil))))))
(def collisions (atom #{}))

(defn update-board [b row column value]
  (let [value (if (= value "")
                nil
                (int value))
        new-row (assoc (nth b row) column value)
        new-b (assoc b row new-row)]
    new-b))

(defn board-cell [row column]
  (fn []
    [:td
     [:select
      {:on-change #(swap! board update-board row column (-> % .-target .-value))
       :value (if-let [v (get-in @board [row column])]
                v
                "X")
       :style {:background-color (if (@collisions [row column])
                                   "red"
                                   "white")}}
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
     (for [i (range 9)]
       [board-cell row i])]))

(defn board-component []
  (fn []
    [:div
     [:button
      {:on-click #(js/alert (str @board))}
      "board"]
     [:button
      {:on-click #(reset! collisions (sudoku/collisions @board))}
      "test collisions"]
     [:button
      {:on-click #(swap! board sudoku/solve)}
      "solve"]
     [:table
      {:style {:border-collapse "collapse"}}
      (for [i (range 9)]
        [board-row i])]]))

(defn home-page []
  (fn []
    (board-component)))


;; -------------------------
;; Initialize app

;;(defn mount-root []
  ;;(reagent/render [current-page] (.getElementById js/document "app")))


(defn init! []
  (reagent/render [home-page] (.getElementById js/document "app")))
