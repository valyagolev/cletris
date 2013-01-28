(ns cletris.board
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [clojure.browser.repl :as repl])
  (:use [jayq.core :only [$ append-to]]
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
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [0 0 0 0 0 0]
                    [1 0 0 0 0 0]
                    [1 1 0 0 0 1]])

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

(defn figure-on-board [board [fy fx] figure]
  "Returns either merged board,
   or :invalid in case of invalid position,
   or :fail in case of game end"

  (let [figure-width (count (figure 0))
        figure-height (count figure)]

    (if (or (> (+ fx (width figure)) board-width)
            (> (+ fy (height figure)) (height board)))
          :invalid

        (reduce
         (fn [brd [y x]]
            (if (= brd :fail) :fail
              (let [by (+ y fy)
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


(def keydowns (.keydownAsObservable cletris.board/$body))

(def move-signal
  (.where
    (.select keydowns #(case (.-keyCode %) 37 :left, 39 :right, nil))
     (complement nil?)))


(def pos-signal
  (.startWith
    (.scan move-signal
         figur-pos-initial
         (fn [[y x] move]
            (let [new-x (case move
                              :left (- x 1)
                              :right (+ x 1))]
              (if (or (< new-x 0) (> (+ (width figure) new-x) board-width)) [y x]
                  [y new-x]))))
    figur-pos-initial))

(def board-signal
  (.where
      (.select pos-signal
           ; board-initial
           (fn [figure-pos]
            (let [new-board (figure-on-board board-initial figure-pos figure)]
              (if (= :invalid new-board) nil
                  new-board))))
      (complement nil?)))



(.subscribe move-signal log)
(.subscribe pos-signal #(log (str %)))
(.subscribe board-signal log)



(.subscribe board-signal #(.html $content (board-template %)))






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



