(ns cletris.board
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [clojure.browser.repl :as repl])
  (:use [jayq.core :only [$ append-to css]]
        [jayq.util :only [log]]))


(defn cartesian-product [xs ys]
  (for [x xs y ys] [x y]))

(def $body ($ "body"))

(defn html-into [$parent content]
  (append-to ($ (hiccups/html content)) $parent))

; (.click (html-into $body [:a {:href "#repl"} "REPL"])
;   #(repl/connect "http://localhost:9000/repl"))

(def $content (html-into $body [:div#content]))

(defhtml board-template [board]
  [:table.board
    (for [row board]
      [:tr
       (for [cell row]
         [:td {:class (case cell
                        2 :figur
                        1 :filld
                        0 :empty)} "."])])])


(def board-initial [[0 0 0 0 0 0]
                    [0 0 0 0 0 1]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [1 0 0 0 0 0]
                    [1 1 1 0 0 1]])

(defn change-2d [coll x y val]
  (assoc coll x
    (assoc (coll x) y
      val)))

(def figure [[1 0]
             [1 1]])

(def figur-pos-initial [0 2])

(defn get-2d [coll y x]
  ((coll y) x))

(defn set-2d [coll y x val]
  (assoc coll y
    (assoc (coll y) x
      val)))

(defn width [col] (count (col 0)))
(defn height [col] (count col))

(def board-width (width board-initial))
(def board-height (height board-initial))

(defn figure-on-board [board [fy fx] figure]
  "Returns either merged board,
   or :invalid in case of invalid position,
   or :fail in case of game end"

  (let [figure-width (count (figure 0))
        figure-height (count figure)]

    (if (or (> (+ fx (width figure)) board-width)
            (> (+ fy (height figure)) board-height))
          :invalid

        (reduce
         (fn [brd [y x]]
            (if (= brd :fail) :fail
              (let [by (+ fy y)
                    bx (+ fx x)
                    fig-val (get-2d figure y x)
                    brd-val (get-2d brd by bx)]
                (case [fig-val brd-val]
                  [1 1] :fail
                  [1 0] (set-2d brd by bx 2)
                  brd))))

                board

               (cartesian-product
                (range (height figure))
                (range (width figure)))))))


(defn validate-position [board [fy fx] figure]
  (let [figure-width (count (figure 0))
        figure-height (count figure)]

    (cond
      (or (> (+ fy (height figure)) (height board))
          (> (+ fx (width figure)) (width board))
          (< fy 0)
          (< fx 0))
            :invalid

      (not-any? (fn [[f b]] (= 1 f b))
                (for [y (range figure-height)
                      x (range figure-width)]

                  [(get-2d figure y x)
                   (get-2d board (+ y fy) (+ x fx))]))
            :good

      :else :fail)))





(defn logged [signal & strs]
  (.subscribe signal #(apply log (str %) " " strs)))


(defn accumulate [signal initial f]
  (.startWith (.scan signal initial f) initial))

(defn filtered [f signal]
  (.where signal f))


(def keydowns (.keydownAsObservable cletris.board/$body))

(def key-move-signal
  (.where
    (.select keydowns #(case (.-keyCode %) 37 :left, 39 :right, nil))
     (complement nil?)))

(def time-move-signal
  (.select ((-> js/Rx .-Observable .-interval) 1000) (constantly :down)))

(logged time-move-signal "time")

(def move-signal ((-> js/Rx .-Observable .-merge) key-move-signal time-move-signal))

(logged move-signal "move")

(def pos-signal
  (accumulate move-signal figur-pos-initial
    (fn [[y x] move]
        (let [new-x (case move
                      :left (- x 1)
                      :right (+ x 1)
                      x)
              new-y (case move
                      :down (+ y 1)
                      y)
              validity  (validate-position board-initial
                                           [new-y new-x] figure)]

          (case validity
            :fail     nil
            :invalid  [y x]
            :good     [new-y new-x]
            :other )))))

(logged pos-signal "pos")

(def board-signal
  (.select pos-signal
    (fn [figure-pos]
      (if (nil? figure-pos) nil
        (figure-on-board board-initial figure-pos figure)))))


(.subscribe board-signal (fn [board]
  (.html $content (if (nil? board) "fail :(" (board-template board)))))






; (.keydown $body (fn [e]
;    (when-let [key (key-codes (.-keyCode e))]
;      (def figur-state (move figur-state key))
;      (def board-state (to-board :top figur-state :bottom filld-state))
;      (draw-board board-state))))

; (js/setInterval (fn []
;     (def figur-state (conj (seq figur-state) (repeat board-width :empty)))
;     (def board-state (to-board :top figur-state :bottom filld-state))
;     (draw-board board-state)
;   ) 1000)



