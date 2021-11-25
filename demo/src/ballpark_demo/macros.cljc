(ns ballpark-demo.macros)

(defmacro timed [label expr]
  `(let [_# (.time js/console ~label)
         result# (do ~expr)]
     (.timeEnd js/console ~label)
     result#))
