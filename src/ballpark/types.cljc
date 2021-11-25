(ns ballpark.types)

#?(:clj
   (set! *warn-on-reflection* true))

;; Protocols

(defprotocol IRange
  (-start [_])
  (-length [_])
  (-end [_]))

(defprotocol IMatch
  ;; return an IRange
  (-match-range [_])
  ;; return an IRange
  (-search-range [_]))

(defprotocol ISearcher
  ;; return a vec of IMatch
  (-search [_ string query]))

(defprotocol IScorer
  ;; given a vec of IMatch
  (-score [_ matches string]))

(defprotocol ToVec
  (-to-vec [_]))

;; Range types

(deftype StringRange #?(:clj [^int start ^int end]
                        :cljs [^number start ^number end])
  IRange
  (-start [_] start)
  (-end [_] end)
  (-length [_] (- end start))

  ToVec
  (-to-vec [_] [start end]))

(deftype Match #?(:clj [^int search-start ^int match-start ^int match-end ^int search-end]
                  :cljs [^number search-start ^number match-start ^number match-end ^number search-end])
  IMatch
  (-match-range [_] (StringRange. match-start match-end))
  (-search-range [_] (StringRange. search-start search-end))

  ToVec
  (-to-vec [_] [[match-start match-end] [search-start search-end]]))

(defn ->range [start end]
  (StringRange. start end))

(defn ->match
  ([search-range match-range]
   (Match. (-start search-range) (-start match-range) (-end match-range) (-end search-range)))
  ([search-start match-start match-end search-end]
   (Match. search-start match-start match-end search-end)))
