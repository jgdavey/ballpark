(ns ballpark.core-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            ;; [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :as tct :refer [defspec]]
            [clojure.string :as str]
            [ballpark.core]))

(def ^:private non-blank-string? #(not (str/blank? %)))

(def ^:private non-blank-string-gen
  (gen/such-that non-blank-string?
                 (gen/fmap #(str/trim (str/replace % #"\t|\n|\r" "")) gen/string-ascii)))

(def ^:private string-range-gen
  (gen/such-that
   (fn [[_ query]]
     (non-blank-string? query))
   (gen/let [string non-blank-string-gen
             length (gen/choose 1 (count string))
             start (gen/choose 0 (- (count string) length))
             query (gen/return (subs string start (+ start length)))]
     [string query])))

(defmacro score-between-0-and-1 [config]
  `(prop/for-all
    [[string# query#] string-range-gen]
    (let [score# (ballpark.core/quick-score string# query# ~config)]
      (is (and score# (<= 0.0 score# 1.0 ))))))

(defspec standard-rank-between-0-and-1 500
  (score-between-0-and-1 (assoc ballpark.core/base-config
                                :scorer ballpark.core/score-standard)))

(defspec quickey-rank-between-0-and-1 250
  (score-between-0-and-1 (assoc ballpark.core/base-config
                                :scorer ballpark.core/score-quickey)))

(defspec quicksilver-rank-between-0-and-1 250
  (score-between-0-and-1 (assoc ballpark.core/base-config
                                :scorer ballpark.core/score-quicksilver)))
