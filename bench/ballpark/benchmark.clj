(ns ballpark.benchmark
  (:require [criterium.core :as crit]
            [ballpark.core :as ballpark]))

(defn do-bench []
  (let [string "Test string"]
    (crit/quick-bench
     (ballpark/quick-score string "sr" ballpark/base-config)))
  (let [string "QuickSilverThing"]
    (crit/quick-bench
     (ballpark/quick-score string "qt" ballpark/base-config)))
  (crit/quick-bench
   (ballpark/quick-score-collection [{:title "Hilfdago" :slug "hill"}
                                     {:title "Hillshire Farms" :slug "hillfarm"}
                                     {:title "HF Intl" :slug "qqqqq"}
                                     {:title "HillFarm2" :slug "adfa"}
                                     {:title "Fairfax Holdings" :slug "ffx"}
                                     {:title "Peoplease, Inc" :slug "ppleas"}]
                                    "hf")))

(comment

  (do-bench)
  )
