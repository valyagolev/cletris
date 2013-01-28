(ns cletris.signals
  (:refer-clojure :exclude [map filter concat reduce])
  (:use [jayq.util :only [log]]))


(def Rx.Observable (.-Observable js/Rx))



(defn logged [signal & strs]
  (.subscribe signal #(apply log (str %) " " strs)))

(defn map [f signal]
  (.select signal f))

(defn filter
  ([signal] (filter (complement nil?) signal))
  ([f signal] (.where signal f)))

(defn concat [& signals]
  (apply (.-merge Rx.Observable) signals))

(defn reduce [f val signal]
  (.startWith (.scan signal val f) val))

(defn constantize [val signal]
  (map (constantly val) signal))

(defn interval [ms value]
  (constantize value ((-> js/Rx .-Observable .-interval) ms)))

(defn marked [mark signal]
  (map #(hash-map mark %) signal))
