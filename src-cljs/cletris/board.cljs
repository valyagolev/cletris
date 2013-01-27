(ns cletris.board
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [clojure.browser.repl :as repl])
  (:use [jayq.core :only [$ append-to]]
        [jayq.util :only [log]]))


(def $body ($ "body"))

(defn html-into [$parent content]
  (append-to ($ (hiccups/html content)) $parent))

(.click (html-into $body [:a {:href "#repl"} "REPL"])
  #(repl/connect "http://localhost:9000/repl"))

(def $content (html-into $body [:div#content]))


(defhtml board-template [board]
  [:table.board
    (for [row board]
      [:tr
       (for [cell row]
         [:td {:class (case cell
                        2 :figur
                        1 :filld
                        0 :empty)}])])])



(defn transpose [board]
  (vec (apply map vector board)))

(defn cycle-1 [coll]
  (conj (vec (rest coll)) (first coll)))



(defn top-direction [direction board]
  (case direction
    :left (transpose board)
    :right (reverse (transpose board))))

(defn untop-direction [direction board]
  (case direction
    :left (transpose board)
    :right (transpose (reverse board))))

(defn move [board direction]
  (let [topped (top-direction direction board)]
    (if (every? #(= % :empty) (first topped))
        (untop-direction direction (cycle-1 topped))
        board)))


(def board [[0 0 0 0 0 0]
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


(def figure [[1 0]
             [1 1]])


(def keydowns (.keydownAsObservable cletris.board/$body))

(def movekeys
  (.where
    (.select keydowns #(case (.-keyCode %) 37 :left, 39 :right, nil))
     (complement nil?)))




(.subscribe movekeys log)

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



