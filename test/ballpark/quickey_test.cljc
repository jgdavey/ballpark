(ns ballpark.quickey-test
  (:require [clojure.test :as test :refer [deftest is]]
            [clojure.string :as str]
            [ballpark.quickey :as qk]
            [ballpark.fuzzy :refer [find-sub-matches]]))

(defn tolerating-eq? [tolerance a b]
  (<= (Math/abs (- a b)) tolerance))

(defn near-enough? [a b]
  (tolerating-eq? 0.00001 a b))

(defn quicksilver-score [string query]
  (let [matches (find-sub-matches string query {:prepare-string str/lower-case
                                                :max-iterations 100})]
    (qk/score-quickey string matches qk/quicksilver-config)))

(defn quickey-score [string query]
  (let [matches (find-sub-matches string query {:prepare-string str/lower-case
                                                :max-iterations 100})]
    (qk/score-quickey string matches qk/quickey-config)))

(defn floor [n places]
  (let [scale (Math/pow 10 places)]
    (-> n
        (* scale)
        Math/floor
        (/ scale))))

(deftest short-string-test
  (let [string "test string"]
    (is (near-enough? (quicksilver-score string "t") 0.90909))
    (is (near-enough? (quicksilver-score string "ts") 0.88182))
    (is (near-enough? (quicksilver-score string "te") 0.91818))
    (is (near-enough? (quicksilver-score string "tet") 0.89091))
    (is (near-enough? (quicksilver-score string "str") 0.91818))
    (is (near-enough? (quicksilver-score string "tstr") 0.93182))
    (is (near-enough? (quicksilver-score string "ng") 0.59091))))

(comment
  (mapv (fn [[query x]]
          (list 'is (list 'near-enough? (list 'score 'string query) x)))
        (partition 2 [])))

(deftest longer-string-test
  (let [string "this is a really long test string for testing"]
    (is (near-enough? (quicksilver-score string "t") 0.90222))
    (is (near-enough? (quicksilver-score string "ts") 0.88666))
    (is (near-enough? (quicksilver-score string "te") 0.80777))
    (is (near-enough? (quicksilver-score string "tet") 0.80111))
    (is (near-enough? (quicksilver-score string "str") 0.78555))
    (is (near-enough? (quicksilver-score string "tstr") 0.78889))
    (is (near-enough? (quicksilver-score string "testi") 0.74))
    (is (near-enough? (quicksilver-score string "for") 0.75888))
    (is (near-enough? (quicksilver-score string "ng") 0.73556))))

(deftest uppercase-score-test
  (is (near-enough? (quicksilver-score "QuicKey" "qk") 0.90714))
  (is (near-enough? (quicksilver-score "quickly" "qk") 0.75714))
  (is (near-enough? (quicksilver-score "WhatIsQuicKey?" "qk") 0.76071))
  (is (near-enough? (quicksilver-score "QuicKey" "QuicKey") 1.0)))

(deftest separator-score-test
  (test/are [string query score] (tolerating-eq?
                                  0.00001
                                  (quicksilver-score string query)
                                  score)
    "react-hot-loader" "rhl" 0.91250
    "are there walls?" "rhl" 0.66875))

(deftest quicksilver-test
  (let [query "bdd"
        expected [["Learning about BDD" 0.8361]
                  ["The bad lands" 0.7653]
                  ["Blessings: drag and drop" 0.7624]
                  ["bevacqua/react-dragula: Drag and drop so simple it hurts" 0.8607]]
        calculated (->> expected (map first) (mapv (juxt identity #(floor (quicksilver-score % query) 4))))]
    (is (= expected calculated))))

(deftest quickey-test
  (let [query "bdd"
        expected [["Learning about BDD" 0.8361]
                  ["The bad lands" 0.4203]
                  ["Blessings: drag and drop" 0.4796]
                  ["bevacqua/react-dragula: Drag and drop so simple it hurts" 0.8607]]
        calculated (->> expected (map first) (mapv (juxt identity #(floor (quickey-score % query) 4))))]
    (is (= expected calculated))))
