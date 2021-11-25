(ns ballpark.fuzzy
  (:require [ballpark.types :as types :refer [->range]]
            [clojure.string :as str]))

#?(:clj
   (set! *warn-on-reflection* true))

(defn range-of-substring [^String string ^String query search-range]
  (let [idx (.indexOf string query ^int (types/-start search-range))]
    (when (<= 0 idx (types/-end search-range))
      (->range idx (unchecked-add idx #?(:clj (.length query) :cljs (.-length query)))))))

(defn find-sub-matches
  ([string query {:keys [prepare-string max-iterations] :as _config}]
   (let [string (prepare-string string)
         query (prepare-string query)
         search-range (->range 0 (count string))
         query-range (->range 0 (count query))
         iterations-v (volatile! (int 0))
         find-sub (fn find-sub [search-range query-range matches]
                    (let [query-len (types/-length query-range)]
                      (cond
                        (> @iterations-v max-iterations) []
                        (> query-len (types/-length search-range)) []
                        (< query-len 1) matches
                        :else (loop [i query-len]
                                (vswap! iterations-v unchecked-inc)
                                (if (pos? i)
                                  (let [loc (types/-start query-range)
                                        query-substring (subs query loc (+ i loc))
                                        match-range (range-of-substring string query-substring
                                                                        (->range (types/-start search-range)
                                                                                 (+ i (- (types/-end search-range) query-len))))
                                        match (when match-range (types/->match search-range match-range))
                                        remain (when match (not-empty
                                                            (find-sub (->range (types/-end match-range) (types/-end search-range))
                                                                      (->range (+ (types/-length match-range) (types/-start query-range))
                                                                               (count query))
                                                                      (conj matches match))))]
                                    (or remain (recur (unchecked-dec i))))
                                  [])))))]
     (find-sub search-range query-range []))))

(def default-config
  {:prepare-string str/lower-case
   :max-iterations 64000})

(deftype FuzzyFinder [config]
  types/ISearcher
  (-search [_ string query] (find-sub-matches string query config)))

(def searcher (->FuzzyFinder default-config))
