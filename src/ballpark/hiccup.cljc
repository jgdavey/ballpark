(ns ballpark.hiccup)

(defn highlight-regions [string matches]
  (if (seq matches)
    (->> matches
         (reduce (fn [acc [s2 e2]]
                   (let [{:keys [region]} (peek acc)
                         [s1 e1] region]
                     (cond-> (pop acc)
                       (not= s1 s2) (conj {:type :span
                                           :region [s1 s2]})
                       :always (into [{:type :strong
                                       :region [s2 e2]}
                                      {:type :span
                                       :region [e2 e1]}]))))
                 [{:type :span
                   :region [0 (count string)]}])
         (into [] (map (fn [{:keys [type region]}]
                         [type (apply subs string region)]))))
    [[:span string]]))

(comment
  (highlight-regions "Fare thee well" [[5 9]])
  ;;=> [[:span "Fare "] [:strong "thee"] [:span " well"]]

  (highlight-regions "Fare thee well" [[0 3] [5 6]])
  ;;=> [[:strong "Far"] [:span "e "] [:strong "t"] [:span "hee well"]]
  )
