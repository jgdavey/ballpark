(ns ballpark.quickey
  (:require [ballpark.util :as util]
            [ballpark.types :as types]))

;; QuicKey scoring
(defn- deduct-range [score rng string special skip-score]
  (reduce (fn [acc idx]
            (- acc
               (if (util/one-of-string? special (util/string-char-at string idx))
                 1
                 skip-score)))
          score
          rng))

(defn remaining-range [match]
  (types/->range (types/-end (types/-match-range match))
                 (types/-end (types/-search-range match))))

(defn offset-range [match]
  (types/->range (types/-start (types/-search-range match))
                 (types/-start (types/-match-range match))))

(defn- use-skip-reduction? [string match-range config]
  (let [string-len (count string)
        match-loc (types/-start match-range)]
    (or (<= string-len (:long-string-length config))
        (< (/ match-loc string-len) 0.15))))

(defn- adjust-remaining-score [remaining-score str-len match-start-pct match-density skipped? match config]
  (let [match-range-discount (if (or skipped?
                                     (and (<= str-len (:long-string-length config))
                                          (<= match-start-pct 0.1)
                                          (>= match-density 0.75)))
                               1
                               match-density)
        match-start-discount (if (or skipped?
                                     (< match-range-discount 0.95))
                               (- 1 match-start-pct)
                               1)]
    (* remaining-score
       (min (types/-length (remaining-range match)) (:long-string-length config))
       match-range-discount
       match-start-discount)))

;; match-start-pct (double (/ (min (-start first-match-range) (count string)) (count string)))
;; match-density (double (/ query-len (- (-end last-match-range) (-start first-match-range))))
(defn score-quickey [string matches config]
  (let [{:keys [word-separators uppercase-letters skipped-score ignored-score
                use-skip-reduction-fn adjust-remaining-score-fn]} config
        first-match-range (types/-match-range (nth matches 0))
        last-match-range (types/-match-range (peek matches))
        query-len (double (transduce (comp (map types/-match-range)
                                           (map types/-length)) + 0 matches))
        match-start-pct (double (/ (min (types/-start first-match-range) (count string)) (count string)))
        match-density (double (/ query-len (- (types/-end last-match-range) (types/-start first-match-range))))
        str-len (count string)
        remaining (remaining-range (peek matches))]
    (loop [acc (if (pos? (types/-length remaining)) ignored-score 1.0)
           ms matches]
      (if-let [match (peek ms)]
        (let [matched-range (types/-match-range match)
              search-range (types/-search-range match)
              base-score (- (types/-end matched-range)
                            (types/-start search-range))
              score (let [skip-reduction? (use-skip-reduction-fn string matched-range config)
                          offset (types/-length (offset-range match))
                          check-skipped? (and (pos? offset) skip-reduction?)
                          [local-score skipped?] (cond
                                                   (and check-skipped?
                                                        (util/one-of-string? word-separators
                                                                             (util/string-char-at string (dec (types/-start matched-range)))))
                                                   [(deduct-range base-score
                                                                  (range (types/-start search-range)
                                                                         (dec (types/-start matched-range)))
                                                                  string
                                                                  word-separators
                                                                  skipped-score)
                                                    true]

                                                   (and check-skipped?
                                                        (util/one-of-string? uppercase-letters
                                                                             (util/string-char-at string (types/-start matched-range))))
                                                   [(deduct-range base-score
                                                                  (range (types/-start search-range)
                                                                         (types/-start matched-range))
                                                                  string
                                                                  uppercase-letters
                                                                  skipped-score)
                                                    true]

                                                   (pos? offset) [(- base-score offset) false]

                                                   :else [base-score true])]
                      (-> acc
                          (adjust-remaining-score-fn str-len match-start-pct match-density skipped? match config)
                          (+ (double local-score))
                          (/ (types/-length search-range))))]
          (recur (double score) (pop ms)))
        acc))))

(deftype QuicKeyScorer [config]
  types/IScorer
  (-score [_ matches string]
    (score-quickey string matches config)))

(def quickey-config
  {:word-separators "-/\\:()<>%._=&[]+ \t\n\r"
   :uppercase-letters "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
   :skipped-score 0.15
   :ignored-score 0.9
   :long-string-length 150
   :use-skip-reduction-fn use-skip-reduction?
   :adjust-remaining-score-fn adjust-remaining-score})

(def quicksilver-config
  {:word-separators "-/\\:()<>%._=&[]+ \t\n\r"
   :uppercase-letters "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
   :skipped-score 0.15
   :ignored-score 0.9
   :long-string-length 150
   :use-skip-reduction-fn (constantly true)
   :adjust-remaining-score-fn
   (fn [remaining-score _str-len _match-start-pct _match-density skipped? match _config]
     (cond-> (* remaining-score (types/-length (remaining-range match)))
       (not skipped?) (+ (/ (types/-length (offset-range match)) 2.0))))})

(def quickey-scorer (->QuicKeyScorer quickey-config))
(def quicksilver-scorer (->QuicKeyScorer quicksilver-config))
