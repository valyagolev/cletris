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


(def $info (html-into $body [:div#info]))

(defn $html [html] ($ (hiccups/html html)))

(defn render-to [element signal]
  (let [subel (append-to ($html [:span]) element)]
    (s/subscribe signal (fn [value]
      (-> (.text subel (str value)) .hide .fadeIn)))))


(def game-state-initial
  {:ended false :figure (random-figure) :board board-initial :score 0})


(def time-move-signal (s/interval-signal 2000 :down))



(def key-move-signal
  (s/filter
    (s/map #(case (.-keyCode %) 37 :left, 39 :right, 40 :down, 32 :rotate, nil)
      (s/callback-signal #(.keydown $body %)))))


(defn signal-panel [& signals]
  (let [table ($html [:table.signal-panel])
        first-cells (map (fn [[k _]] [:td k] ) (partition 2 signals))
        first-row ($html [:tr first-cells])
        second-cells (map (fn [[_ s]]
                            (let [cell ($html [:td])]
                            (render-to cell s)
                            cell))
                          (partition 2 signals))
        second-row (reduce #(.append %1 %2) ($html [:tr]) second-cells)]
    (.append (.append table first-row) second-row)))

(def move-signal
  (s/map #(hash-map :move %)
    (s/concat time-move-signal key-move-signal)))


(.append
  $body
  (signal-panel
    "Key Move" key-move-signal
    "Time Move" time-move-signal
    "Move" move-signal))



; (def restart-signal
;   (signal/marked :restart (.clickAsObservable $body)))


; (defn freeze [figure board]
;   (let [new-board (into figure board)
;         full-lines (find-full-lines new-board)]
;     {:full-lines (count full-lines)
;      :new-board (reduce remove-full-line new-board full-lines)}))


; (defn figure-stuck [{:keys [figure board score] :as state}]
;   (log "current-score" score)
;   (let [newf                           (random-figure)
;         {:keys [full-lines new-board]} (freeze figure board)
;         new-state (if (some new-board newf) (assoc state :ended true)
;                                             (assoc state
;                                               :figure newf
;                                               :board new-board
;                                               :hello "hey"
;                                               :score (+ score full-lines)))]
;     (log "current-full-lines" full-lines)
;     (assoc new-state :score2 full-lines)))




; (defn game-state-transition [{:keys [ended figure board score] :as state}
;                              {:keys [restart move]             :as action}]
;   (cond
;     restart                                           game-state-initial
;     ended                                             state
;     move (let [newf (move-figure figure move)
;                invalid? (invalid-figure? board newf)]
;             (cond (and invalid? (= move :down))       (figure-stuck state)
;                   invalid?                            state
;                   :else                               (assoc state :figure newf)))
;     :else                                             state))


; (def game-state-signal
;   (signal/reduce game-state-transition
;     game-state-initial (signal/concat move-signal restart-signal)))

; (defn draw-state [{:keys [ended figure board] :as state}]
;   (log (str state))
;   (.html $content
;     (if ended (hiccups/html [:h1 "fail :("])
;         (board-template board figure))))

; (.subscribe game-state-signal draw-state)


; (def $state (html-into $body [:div#state]))

; (.subscribe game-state-signal #(.text $state (str %)))
