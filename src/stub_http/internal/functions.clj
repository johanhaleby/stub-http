(ns stub-http.internal.functions)

(defn map-kv [fk fv m]
  "Maps the keys and values in a map with the supplied functions.
  fk = the function for the keys
  fv = the function for the values"
  (into {} (for [[k v] m] [(fk k) (fv v)])))

(defn substring-before [str before]
  "Return the substring of string <str> before string <before>. If no match then <str> is returned."
  (let [index-of-before (.indexOf str before)]
    (if (= -1 index-of-before)
      str
      (.substring str 0 index-of-before))))

(def not-nil? (complement nil?))

