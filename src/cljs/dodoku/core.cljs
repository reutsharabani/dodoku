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
  (let [hover? (atom false)]
    (fn []
      [:td
       [:select
        {:on-change #(swap! board update-board row column (-> % .-target .-value))
         :value (if-let [v (get-in @board [row column])]
                  v
                  "X")
         :on-mouse-over #(swap! hover? not)
         :on-mouse-out #(swap! hover? not)
         :style (merge (square-border row column)
                       {:-webkit-appearance "none"
                        :width "100%"
                        :text-align "center"
                        :-moz-appearance "none"
                        :appearance "none"
                        :background-color (if (@collisions [row column])
                                            (if @hover?
                                              "yellow"
                                              "red")
                                            (if @hover?
                                              "lightgray"
                                              "white"))})}
        [:option
         {:key ""
          :value ""} "X"]
        (for [v (range 1 10)]
          [:option
           {:key v
            :value v}
           v])]])))

(defn board-row [row]
  (fn []
    [:tr
     (for [column (range 9)]
       ^{:key [row column]} [board-cell row column])]))
(def buttons-style {:out {:background-color "white"
                          :color "black"}
                    :hover {:background-color "black"
                            :color "white"}})

(defn board-component []
  (let [selected-style (atom {:test :out
                              :solve :out})]
    (fn []
      [:div
       {:align "center"
        :style {:width "70%" :margin "auto"}}
       [:button
        {:on-click #(reset! collisions (sudoku/collisions @board))
         :on-mouse-over #(swap! selected-style assoc :test :hover)
         :on-mouse-out #(swap! selected-style assoc :test :out)
         :style (merge ((:test @selected-style) buttons-style)
                       {:width "50%"})}
        "TEST COLLISIONS"]
       [:button
        {:on-click #(swap! board sudoku/solve)
         :on-mouse-over #(swap! selected-style assoc :solve :hover)
         :on-mouse-out #(swap! selected-style assoc :solve :out)
         :style (merge ((:solve @selected-style) buttons-style)
                       {:width "50%"})}
        "SOLVE"]
       [:table
        {:style {:width "100%"
                 :border "2px solid gray"}}
        [:tbody
         {:style {:border-collapse "collapse"}}
         (for [i (range 9)]
           ^{:key i} [board-row i])]]])))

(defn home-page []
  (fn []
    (board-component)))


;; -------------------------
;; Initialize app

;;(defn mount-root []
;;(reagent/render [current-page] (.getElementById js/document "app")))


(defn init! []
  (reagent/render [home-page] (.getElementById js/document "app")))
