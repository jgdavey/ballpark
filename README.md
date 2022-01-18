# Ballpark

Fuzzy string matching and ranking.

This library was originally inspired by [fwextensions/quick-score](https://github.com/fwextensions/quick-score), which itself was inspired by QuickSilver.app. The algorithm for quick-score (used in the QuicKey browser extension), as well as that for QuickSilver are provided in this library, but are not the default.

The default searcher requires that query characters appear in the order that they appear in the source string to be considered a "match".

The main use-case for this style of searching is to jump as quickly as possible, especially for keyboard-based navigation and commands.


## Example usage

Let's say we have a bunch of restaurants:

```clojure
(def restaurants
  [{:name "McRonald's" :description "Fast and cheap"}
   {:name "Snapplebee's" :description "Better than boxed mashed potatoes"}
   {:name "Chris' Ruth" :description "Steakhouse famous for its prices"}
   {:name "Dairy King" :description "They apparently make burgers, too"}
   {:name "Whiskey Barrel" :description "Hipster paradise"}
   {:name "TGIMonday's" :description "It's never the weekend here"}
   {:name "Mr. Pork" :description "Barbeque and nothing else"}
   {:name "Pete's Za" :description "Saucy"}
   {:name "Java Beans" :description "Take a byte out of your day"}])
```

Once we require the library,

```clojure
(require '[ballpark.core :as bp])
```

We can query `mr`, and we'd get the following results:

```clojure
(bp/quick-score-collection restaurants "mr")
```

```clojure
[{:item {:name "Mr. Pork", :description "Barbeque and nothing else"},
  :matches {:name [[0 2]], :description []},
  :scores {:name 0.925, :description 0.0},
  :score 0.925,
  :key :name}
 {:item {:name "McRonald's", :description "Fast and cheap"},
  :matches {:name [[0 1] [2 3]], :description []},
  :scores {:name 0.8783333333333334, :description 0.0},
  :score 0.8783333333333334,
  :key :name}
 {:item
  {:name "Dairy King",
   :description "They apparently make burgers, too"},
  :matches {:name [], :description [[16 17] [23 24]]},
  :scores {:name 0.0, :description 0.1349623152365598},
  :score 0.1349623152365598,
  :key :description}
 {:item
  {:name "Chris' Ruth",
   :description "Steakhouse famous for its prices"},
  :matches {:name [], :description [[13 14] [20 21]]},
  :scores {:name 0.0, :description 0.019235786646604528},
  :score 0.019235786646604528,
  :key :description}]

```

Oh, look at that! The first item returned is "Mr. Pork", which makes sense. It scored relatively high as well. After that is "McRonald's", which also scored fairly well because "M" and "R" are capital letters.

After that we see 2 results that seems a bit less relevant. Looking at the `:key` field, both of these matched the `:description`, and their scores are pretty low. There are a couple ways to filter. To exclude descriptions from being searched at all, provide a `keyseq` argument to `quick-score-collection`:

```clojure
(bp/quick-score-collection restaurants [:name] "mr")
```

```clojure
[{:item {:name "Mr. Pork", :description "Barbeque and nothing else"},
  :matches {:name [[0 2]]},
  :scores {:name 0.925},
  :score 0.925,
  :key :name}
 {:item {:name "McRonald's", :description "Fast and cheap"},
  :matches {:name [[0 1] [2 3]]},
  :scores {:name 0.8783333333333334},
  :score 0.8783333333333334,
  :key :name}]

```

Okay, nice. Now only the first two show up.


## Highlighting results

If using hiccup, we can render matching sections of the source strings like this:

```clojure
(require '[ballpark.hiccup])
```

```clojure
(into
 [:ul]
 (for [result (bp/quick-score-collection restaurants [:name] "mr")]
   (into [:li]
         (ballpark.hiccup/highlight-regions (get-in result [:item :name])
                                            (get-in result [:matches :name])))))
```

```clojure
[:ul
 [:li [:strong "Mr"] [:span ". Pork"]]
 [:li [:strong "M"] [:span "c"] [:strong "R"] [:span "onald's"]]]

```


## License

Copyright © 2022 Joshua Davey

Distributed under the Eclipse Public License version 1.0.
