(ns ballpark.core
  (:require [clojure.string :as str]
            [ballpark.fuzzy]
            [ballpark.types :as types]
            [ballpark.quickey]
            [ballpark.standard]
            [ballpark.util :as util]))

#?(:clj
   (set! *warn-on-reflection* true))

(def searcher ballpark.fuzzy/searcher)

(def score-standard ballpark.standard/scorer)
(def score-quickey ballpark.quickey/quickey-scorer)
(def score-quicksilver ballpark.quickey/quicksilver-scorer)

(def base-config
  {:minimum-score 0
   :searcher searcher
   :scorer score-standard})

(defn quick-score
  [string query config]
  (if (str/blank? query)
    (:empty-query-score config)
    (let [searcher (get config :searcher searcher)
          scorer (get config :scorer score-standard)
          matches (types/-search searcher string query)]
      (if (seq matches)
        (types/-score scorer matches string)
        0.0))))

(def ^:private match-range-xf (comp (keep types/-match-range)
                                    (map types/-to-vec)))

(defn quick-score-keys
  ([m keyseq query config]
   (if (seq query)
     (let [searcher (get config :searcher searcher)
           scorer (get config :scorer score-standard)
           results (into [] (map (fn find-and-score [k]
                                   (let [value (get m k)
                                         ms (when value (types/-search searcher value query))]
                                     {:key k
                                      :value value
                                      :matches (into [] match-range-xf ms)
                                      :score (if (seq ms) (types/-score scorer ms value) 0.0)})))
                         keyseq)
           best-result (apply max-key :score results)]
       {:item m
        :matches (persistent! (reduce #(assoc! %1 (:key %2) (:matches %2))
                                      (transient {}) results))
        :scores (persistent! (reduce #(assoc! %1 (:key %2) (:score %2))
                                     (transient {}) results))
        :score (:score best-result)
        :key (:key best-result)})
     {:item m})))

(defn quick-score-collection
  ([coll query]
   (let [sample (first coll)
         keyseq (into [] (filter #(string? (get sample %))) (some-> sample keys))]
     (if (seq keyseq)
       (quick-score-collection coll keyseq query base-config)
       [])))
  ([coll keyseq query]
   (quick-score-collection coll keyseq query base-config))
  ([coll keyseq query config]
   (into [] (comp (map #(quick-score-keys % keyseq query config))
                  (remove #(some-> % :score (<= (:minimum-score config))))
                  (if (seq query)
                    (util/sorting-by :score (fn* [a b] (if (< a b) 1 -1)))
                    identity)
                  (if (:limit config) (take (:limit config)) identity))
         coll)))
