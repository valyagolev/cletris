(ns cletris.game
  (:use-macros [hiccups.core :only [defhtml]])
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [clojure.browser.repl :as repl]
            [cletris.signals :as signal])
  (:use [jayq.core :only [$ append-to css]]
        [jayq.util :only [log]]
        [cletris.board :only [figure-initial
                              board-initial
                              valid-figure?
                              fail-figure?
                              move-figure
                              board-template]]))

(defn html-into [$parent content]
  (append-to ($ (hiccups/html content)) $parent))

(def $body ($ "body"))
(def $content (html-into $body [:div#content]))




(def game-state-initial
  {:ended false :figure figure-initial})


(def time-move-signal
  (signal/interval 500 :down))

(def key-move-signal
  (signal/filter
    (signal/map #(case (.-keyCode %) 37 :left, 39 :right, 40 :down, nil)
                (.keydownAsObservable $body))))

(def move-signal
  (signal/marked :move
    (signal/concat time-move-signal key-move-signal)))


(def restart-signal
  (signal/marked :restart (.clickAsObservable $body)))



(defn game-state-transition [{:keys [ended figure] :as state}
                             {:keys [restart move] :as action}]

  (cond
    restart                                                         game-state-initial
    ended                                                           state
    move        (let [new-figure (move-figure figure move)]
                  (cond (fail-figure? board-initial new-figure)     {:ended true}
                        (not (valid-figure? new-figure))            state
                        :else                                       (assoc state :figure new-figure)))
    :else                                                           state))


(def game-state-signal
  (signal/reduce game-state-transition
    game-state-initial (signal/concat move-signal restart-signal)))


(defn draw-state [state]
  (.html $content
    (if (:ended state) (hiccups/html [:h1 "fail :("])
        (board-template board-initial (:figure state)))))

(.subscribe game-state-signal draw-state)
