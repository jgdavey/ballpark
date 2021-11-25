(ns ballpark.standard
  (:require [ballpark.util :as util]
            [ballpark.types :as types]))


(defn- skip-score [string chars lower upper]
  (if (< lower upper)
    (loop [skipped 0
           i lower]
      (if (< i upper)
        (recur (if (util/one-of-string? chars (util/string-char-at string i))
                 (unchecked-inc skipped)
                 skipped)
               (unchecked-inc i))
        (let [length (- upper lower)]
          (/ (- length skipped) length))))
    1.0))

;; Ballpark standard scoring
(defn score-standard [string matches config]
  (let [{:keys [word-separators uppercase-letters long-string-length]} config
        last-match-range (types/-match-range (peek matches))
        first-match-range (types/-match-range (nth matches 0))
        query-len (double (transduce (comp (map types/-match-range)
                                           (map types/-length)) + 0 matches))
        str-len (min (count string) long-string-length)
        end (min (types/-end last-match-range) (dec long-string-length))
        remain-score (if (> end str-len)
                       0.9
                       (let [ratio (/ (double end) (double str-len))]
                         (/ (+ ratio 9.0) 10.0)))
        start-score (let [distance (min (types/-start first-match-range) long-string-length)
                          ratio (/ distance (util/clamp str-len 16.0 long-string-length))]
                      (-> ratio
                          util/inverse-square-curve
                          (+ 1.0)
                          (/ 2.0)))
        match-density (double (/ query-len (- (types/-end last-match-range) (types/-start first-match-range))))
        density-score (-> match-density
                          util/square-curve
                          (+ 1.0)
                          (/ 2.0))
        segment-score (reduce (fn [acc m]
                                (let [search-range (types/-search-range m)
                                      match-range (types/-match-range m)
                                      search-loc (types/-start search-range)
                                      loc (types/-start match-range)
                                      score (cond
                                              (= loc search-loc)
                                              1.0

                                              (util/one-of-string? word-separators (util/string-char-at string (dec loc)))
                                              (skip-score string word-separators search-loc (dec loc))

                                              (util/one-of-string? word-separators (util/string-char-at string loc))
                                              (skip-score string word-separators search-loc loc)

                                              (util/one-of-string? uppercase-letters (util/string-char-at string loc))
                                              (skip-score string uppercase-letters search-loc loc)

                                              :else
                                              (-> (/ (types/-length match-range)
                                                     (+ (- (types/-end match-range) search-loc) 1))
                                                  util/square-curve
                                                  (util/clamp 0.1 0.9)))]
                                  (* acc score)))
                              1.0
                              matches)]
    (* segment-score remain-score start-score density-score)))

(def default-config
  {:word-separators "-/\\:()<>%._=&[]+ \t\n\r"
   :uppercase-letters "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
   :long-string-length 150})

(deftype StandardScorer [config]
  types/IScorer
  (-score [_ matches string]
    (score-standard string matches config)))

(def scorer (->StandardScorer default-config))

(comment

  (require 'ballpark.fuzzy)

  (defn qs [string query]
    (let [matches (types/-search ballpark.fuzzy/searcher string query)]
      (score-standard string matches default-config)))

  (qs "jQuery Zoom"
      "zom")

  (qs "RethinkDB: the open-source database for the realtime web"
      "redb")

  (qs "AppJS" "js")

  (qs "marsdb" "rdb")

  (qs "Relay | A JavaScript framework for building data-driven"
      "rjs")

  (qs "reactabular - Spectacular tables for React.js"
      "rjs")

  (qs "lunr.js - A bit like Solr, but much smaller and not as bright"
      "unr")

  )
