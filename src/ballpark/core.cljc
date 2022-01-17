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
  "Given a string a query (and optional config), returns a score between
  0.0 and 1.0, where 1.0 is the highest (often an exact match), and
  0.0 is a non-match. Whether or not a query matches (and what counts
  as a match) will depend on the :searcher, while the score will
  depend on the :scorer. Both of these can be set in the config.

  The default :searcher is a linear fuzzy searcher: characters from
  the query must appear in order in the string to count as a match.

  The default :scorer is ballpark.standard/scorer"
  ([string query]
   (quick-score string query base-config))
  ([string query config]
   (if (str/blank? query)
     (:empty-query-score config)
     (let [searcher (get config :searcher searcher)
           scorer (get config :scorer score-standard)
           matches (types/-search searcher string query)]
       (if (seq matches)
         (types/-score scorer matches string)
         0.0)))))

(def ^:private match-range-xf (comp (keep types/-match-range)
                                    (map types/-to-vec)))

(defn quick-score-keys
  "Given a map `m`, keys to search (`keyseq`), a query, and config,
  returns a new scored map, with the original map nested under
  `:item`. For each key in the `keyseq`, values are retrieved from the
  map with `get`.

  In the return value map, the following keys:

      :item       The original map (m)

      :matches    A map of key => match ranges, for each of
                  the given keys in the keyseq

      :scores     A map of key => score, for each of the
                  given keys in the keyseq

      :score      The highest score from the scores key

      :key        The key that yielded the highest score

  When `query` is empty, returns a map with just the `:item` key, and
  no score or match keys."
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
  "Given a collection of maps and query, return a sorted collection,
  where the highest-scoring items are first. Each map in the return
  has the original nested under `:item`, and other keys are set as by
  `quick-score-keys`.

  By default, all keys with detected string values will be scored, but
  this can be changed by providing a `keyseq`.

  An optional config can be supplied, but defaults to `base-config`."
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
