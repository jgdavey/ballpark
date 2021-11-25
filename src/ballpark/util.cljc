(ns ballpark.util)

(defn string-char-at [^String string position]
  (subs string position (inc position)))

(defn one-of-string? [^String string-collection ^String string-char]
  (> (.indexOf string-collection string-char) -1))

(defn square-curve [^double n]
  (let [x (- 1.0 (min n 1.0))]
    (- 1.0 (* x x))))

(defn inverse-square-curve [^double n]
  (- 1.0 (* n n)))

(defn clamp [^double n ^double lower ^double upper]
  (max (min n upper) lower))

(defn sorting
  ([] (sorting compare))
  ([cmp]
   (fn [rf]
     (let [buf #?(:clj (java.util.ArrayList.) :cljs #js [])]
       (fn
         ([] (rf))
         ([acc] (rf (reduce rf acc (doto buf #?(:clj (java.util.Collections/sort cmp) :cljs (.sort cmp))))))
         ([acc x] (#?(:clj .add :cljs .push) buf x) acc))))))

(defn sorting-by
  ([kfn] (sorting-by kfn compare))
  ([kfn cmp]
   (sorting (fn [a b]
              #?(:clj (.compare ^java.util.Comparator cmp (kfn a) (kfn b))
                 :cljs (cmp (kfn a) (kfn b)))))))
