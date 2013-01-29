(ns cletris.game
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [clojure.browser.repl :as repl]
            [cletris.signals :as signal]
            [water.core :as s])
  (:use [jayq.core :only [$ append-to css]]
        [jayq.util :only [log]]
        [cletris.board :only [random-figure
                              board-initial
                              invalid-figure?
                              figure-center
                              move-figure
                              board-template
                              find-full-lines
                              remove-full-line]]))




(defn html-into [$parent content]
  (append-to ($ (hiccups/html content)) $parent))

(def $body ($ "body"))
(def $content (html-into $body [:div#content]))


(def game-state-initial
  {:ended false :figure (random-figure) :board board-initial :score 0})


(def time-move-signal
  (signal/interval 500 :down))

(def key-move-signal
  (signal/filter
    (signal/map #(case (.-keyCode %) 37 :left, 39 :right, 40 :down, 32 :rotate, nil)
                (.keydownAsObservable $body))))


(def move-signal
  (signal/marked :move
    (signal/concat time-move-signal key-move-signal)))


(def restart-signal
  (signal/marked :restart (.clickAsObservable $body)))


(defn freeze [figure board]
  (let [new-board (into figure board)
        full-lines (find-full-lines new-board)]
    {:full-lines (count full-lines)
     :new-board (reduce remove-full-line new-board full-lines)}))


(defn figure-stuck [{:keys [figure board score] :as state}]
  (log "current-score" score)
  (let [newf                           (random-figure)
        {:keys [full-lines new-board]} (freeze figure board)
        new-state (if (some new-board newf) (assoc state :ended true)
                                            (assoc state
                                              :figure newf
                                              :board new-board
                                              :hello "hey"
                                              :score (+ score full-lines)))]
    (log "current-full-lines" full-lines)
    (assoc new-state :score2 full-lines)))




(defn game-state-transition [{:keys [ended figure board score] :as state}
                             {:keys [restart move]             :as action}]
  (cond
    restart                                           game-state-initial
    ended                                             state
    move (let [newf (move-figure figure move)
               invalid? (invalid-figure? board newf)]
            (cond (and invalid? (= move :down))       (figure-stuck state)
                  invalid?                            state
                  :else                               (assoc state :figure newf)))
    :else                                             state))


(def game-state-signal
  (signal/reduce game-state-transition
    game-state-initial (signal/concat move-signal restart-signal)))

(defn draw-state [{:keys [ended figure board] :as state}]
  (log (str state))
  (.html $content
    (if ended (hiccups/html [:h1 "fail :("])
        (board-template board figure))))

(.subscribe game-state-signal draw-state)


; (signal/logged game-state-signal)


(def $state (html-into $body [:div#state]))

(.subscribe game-state-signal #(.text $state (str %)))
