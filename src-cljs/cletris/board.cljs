(ns cletris.board
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$ append-to]]
        [jayq.util :only [log]]
        [clojure.string :only [split]]))

(def board-width 10)
(def board-height 13)

(def board-initial #{})

(defn figure-center-by-dimension [figure dimension]
  (let [cs (map #(% dimension) figure)
        mx (apply max cs)
        mn (apply min cs)
        sz (- mx mn)] (+ mn (quot sz 2))))


(defn figure-center [figure]
  (map #(figure-center-by-dimension figure %) [0 1]))


(defn transition [figure move]
  (if (= :rotate move)

    (let [[cy cx] (figure-center figure)]
      (log "center" (str [cx cy]))
      (fn [[fy fx]]
        (let [my (- fy cy)
              mx (- fx cx)
              ny mx
              nx (- my)
              ry (+ ny cy)
              rx (+ nx cx)]
              [ry rx])))




      (let [[y x] (case move
                     :left  [0 -1]
                     :right [0 1]
                     :down  [1 0])]
        (fn [[fy fx]] [(+ fy y) (+ fx x)]))))

(defn move-figure [figure move]
  (set (map (transition figure move) figure)))

(defn invalid-point? [[y x]]
  (or (< y 0) (< x 0) (>= y board-height) (>= x board-width)))

(defn invalid-figure? [board figure]
  (or (some invalid-point? figure) (some board figure)))

(defn indexed [coll]
  (map vector (iterate inc 0) coll))


(defn figure-from-str [str]
  (set (for [[i line] (indexed (split str #"\s+"))
        [j val]  (indexed line)
        :when (= \1 val)] [i j])))

(def figures (map figure-from-str [
  "1111",
  "100
   111",
  "001
   111",
  "11
   11",
  "011
   110",
  "010
   111",
  "110
   011"]))

(defn random-figure []
  (rand-nth figures))

(defn full-line? [line board]
  (every? board (for [x (range board-width)] [line x])))

(defn find-full-lines [board]
  (for [line (range board-height) :when (full-line? line board)] line))

(defn remove-full-line [board line]
  (set
    (map (fn [[y x]] (if (< y line) [(+ y 1) x] [y x]))
      (filter (fn [[y x]] (not= y line))
        board))))

(defn freeze [figure board]
  (let [new-board (into figure board)]
    (reduce remove-full-line new-board (find-full-lines new-board))))


(defhtml board-template [board figure]
  [:table.board
    (for [row (range board-height)]
      [:tr
       (for [col (range board-width)]
         [:td {:class (cond (contains? board [row col]) :filld
                            (contains? figure [row col]) :figure
                            :else nil)}
              (str row "," col)])])])
