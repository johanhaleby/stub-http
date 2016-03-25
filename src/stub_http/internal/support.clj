(ns stub-http.internal.support)

(defn map-kv [fk fv m]
  "Maps the keys and values in a map with the supplied functions.
  fk = the function for the keys
  fv = the function for the values"
  (into {} (for [[k v] m] [(fk k) (fv v)])))

(defn keywordize-keys
  "Recursively transforms all map keys from strings to keywords."
  [m]
  (let [f (fn [[k v]] (if (string? k) [(keyword k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn substring-before [str before]
  "Return the substring of string <str> before string <before>. If no match then <str> is returned."
  (let [index-of-before (.indexOf str before)]
    (if (= -1 index-of-before)
      str
      (.substring str 0 index-of-before))))

