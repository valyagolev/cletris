(ns cletris.board
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$ append-to]]
        [jayq.util :only [log]]))

(def board-width 6)
(def board-height 12)

(def board-initial #{ [11 1] [10 1] [11 2] [11 3] [11 5] })
(def figure-initial #{ [0 2] [1 2] [1 3] })

(defn transition [[y x]]
  (fn [[fy fx]] [(+ fy y) (+ fx x)]))

(defn move-figure [figure move]
  (let [deltas (case move
                     :left  [0 -1]
                     :right [0 1]
                     :down  [1 0])]
    (set (map (transition deltas) figure))))

(defn invalid-point? [[y x]]
  (or (< y 0) (< x 0) (>= y board-height) (>= x board-width)))

(defn invalid-figure? [board figure]
  (or (some invalid-point? figure) (some board figure)))

(defhtml board-template [board figure]
  [:table.board
    (for [row (range board-height)]
      [:tr
       (for [col (range board-width)]
         [:td {:class (cond (contains? board [row col]) :filld
                            (contains? figure [row col]) :figure
                            :else nil)}
              (str row "," col)])])])
