(ns clj-odbp.serialize.binary.record
  (:require [clj-odbp.constants :as const]
            [clj-odbp.serialize.binary.common :as c]
            [clj-odbp.serialize.binary.int :as i]
            [clj-odbp.serialize.binary.varint :as v]
            [clojure.string :as string])
  (:import [java.io ByteArrayOutputStream DataOutputStream]
           [java.text SimpleDateFormat]))

(defprotocol OrientType
  (getDataType [value])
  (serialize [value] [value offset]))

(defn short-type
  [value]
  (byte-array (v/varint-unsigned value)))

(extend-type java.lang.Short
  OrientType
  (getDataType [value]
    (byte 2))
  (serialize
    ([value] (short-type value))
    ([value offset] (serialize value))))

(defn integer-type
  [value]
  (byte-array (v/varint-unsigned value)))

(extend-type java.lang.Integer
  OrientType
  (getDataType [value]
    (byte 1))
  (serialize
    ([value] (integer-type value))
    ([value offset] (serialize value))))

(defn long-type
  [value]
  (byte-array (v/varint-unsigned value)))

(extend-type java.lang.Long
  OrientType
  (getDataType [value]
    (byte 3))
  (serialize
    ([value] (long-type value))
    ([value offset] (serialize value))))

(defn byte-type
  [value]
  value)

(extend-type java.lang.Byte
  OrientType
  (getDataType [value]
    (byte 17))
  (serialize
    ([value] (byte-type value))
    ([value offset] (serialize value))))

(defn boolean-type
  [value]
  (if value
    (byte 1)
    (byte 0)))

(extend-type java.lang.Boolean
  OrientType
  (getDataType [value]
    (byte 0))
  (serialize
    ([value] (boolean-type value))
    ([value offset] (serialize value))))

(defn float-type
  [value]
  (let [bos (ByteArrayOutputStream. 4)
        dos (DataOutputStream. bos)]
    (.writeFloat dos value)
    (.toByteArray bos)))

(extend-type java.lang.Float
  OrientType
  (getDataType [value]
    (byte 4))
  (serialize
    ([value] (float-type value))
    ([value offset] (serialize value))))

(defn double-type
  [value]
  (let [bos (ByteArrayOutputStream. 8)
        dos (DataOutputStream. bos)]
    (.writeDouble dos value)
    (.toByteArray bos)))

(extend-type java.lang.Double
  OrientType
  (getDataType [value]
    (byte 5))
  (serialize
    ([value] (double-type value))
    ([value offset] (serialize value))))

(extend-type java.math.BigDecimal
  OrientType
  (serialize [value]
    (-> value
        (.multiply (bigdec (Math/pow 10 (.scale value))))
        .toBigInteger
        .toByteArray)))

(defn string-type
  [value]
  (let [bytes (.getBytes value)]
    (c/bytes-type bytes)))

(extend-type java.lang.String
  OrientType
  (getDataType [value]
    (byte 7))
  (serialize
    ([value] (string-type value))
    ([value offset] (serialize value))))

(defn keyword-type
  [value]
  (string-type (name value)))

(extend-type clojure.lang.Keyword
  OrientType
  (getDataType [value]
    (byte 7))
  (serialize
    ([value] (keyword-type value))
    ([value offset] (serialize value))))

(defn coll-type
  [value]
  (map serialize value))

(extend-type clojure.lang.PersistentList
  OrientType
  (serialize
    ([value] (coll-type value))
    ([value offset] (serialize value))))

(extend-type clojure.lang.PersistentVector
  OrientType
  (serialize
    ([value] (coll-type value))
    ([value offset] (serialize value))))

(extend-type clojure.lang.PersistentHashSet
  OrientType
  (serialize
    ([value] (coll-type value))
    ([value offset] (serialize value))))

(defn map-type
  [value]
  (->> value
       vec
       flatten
       (map serialize)))

(extend-type clojure.lang.PersistentArrayMap
  OrientType
  (serialize
    ([value] (map-type value))
    ([value offset] (serialize value))))

(deftype OrientInt32 [value]
  OrientType
  (serialize [this]
    (i/int32 (int value)))
  (serialize [this offset]
    (serialize this)))

(defn orient-int32
  [value]
  (->OrientInt32 value))

(deftype OrientInt64 [value]
  OrientType
  (serialize [this]
    (i/int64 value))
  (serialize [this offset]
    (serialize this)))

(defn orient-int64
  [value]
  (->OrientInt64 value))

(deftype OrientDateTime [value]
  OrientType
  (getDataType [this]
    (byte 6))
  (serialize [this]
    (long-type (.getTime value)))
  (serialize [this offset]
    (serialize this)))

(defn orient-date-time
  [value]
  (->OrientDateTime value))

(deftype OrientBinary [value]
  OrientType
  (getDataType [this]
    (byte 8))
  (serialize [this]
    (c/bytes-type value))
  (serialize [this offset]
    (serialize this)))

(defn orient-binary
  [value]
  (->OrientBinary value))

(deftype OrientEmbeddedList [value]
  OrientType
  (getDataType [this]
    (byte 10))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (count value)
          size-varint (byte-array (v/varint-unsigned size))
          size-varint-len (count size-varint)
          serialized-items (map serialize value)
          serialized-items-len (count serialized-items)]
      (.write dos size-varint 0 size-varint-len)
      (.writeByte dos (byte 23))
      (doall (map #(.write dos % 0 (count %)) serialized-items))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-embedded-list
  [value]
  (->OrientEmbeddedList value))

(deftype OrientEmbeddedSet [value]
  OrientType
  (getDataType [this]
    (byte 11))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (count value)
          size-varint (byte-array (v/varint-unsigned size))
          size-varint-len (count size-varint)
          serialized-items (map serialize value)
          serialized-items-len (count serialized-items)]
      (.write dos size-varint 0 size-varint-len)
      (.writeByte dos (byte 23))
      (doall (map #(.write dos % 0 (count %)) serialized-items))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-embedded-set
  [value]
  (->OrientEmbeddedSet value))

(deftype OrientLink [cluster-id record-position]
  OrientType
  (getDataType [this]
    (byte 13))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          cid-varint (byte-array (v/varint-unsigned cluster-id))
          rpos-varint (byte-array (v/varint-unsigned record-position))]
      (.write dos cid-varint 0 (count cid-varint))
      (.write dos rpos-varint 0 (count rpos-varint))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-link
  [cluster-id record-position]
  (->OrientLink cluster-id record-position))

(deftype OrientLinkList [value]
  OrientType
  (getDataType [this]
    (byte 14))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (count value)
          size-varint (byte-array (v/varint-unsigned size))
          size-varint-len (count size-varint)
          serialized-items (map serialize value)
          serialized-items-len (count serialized-items)]
      (.write dos size-varint 0 size-varint-len)
      (doall (map #(.write dos % 0 (count %)) serialized-items))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-link-list
  [value]
  (->OrientLinkList value))

(deftype OrientLinkSet [value]
  OrientType
  (getDataType [this]
    (byte 15))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (count value)
          size-varint (byte-array (v/varint-unsigned size))
          size-varint-len (count size-varint)
          serialized-items (map serialize value)
          serialized-items-len (count serialized-items)]
      (.write dos size-varint 0 size-varint-len)
      (doall (map #(.write dos % 0 (count %)) serialized-items))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-link-set
  [value]
  (->OrientLinkSet value))

(defn serialize-key-value
  [k v]
  (if-not (map? v)
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          key-type (getDataType k)
          key-value (serialize k)
          link (serialize v)]
      (.writeByte dos key-type)
      (.write dos key-value 0 (count key-value))
      (.write dos link 0 (count link))
      (.toByteArray bos))))

(deftype OrientLinkMap [value]
  OrientType
  (getDataType [this]
    (byte 16))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (byte-array (v/varint-unsigned (count value)))
          key-values (doall (for [[k v] value]
                              (serialize-key-value k v)))]
      (.write dos size 0 (count size))
      (doall (map #(.write dos % 0 (count %)) key-values))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-link-map
  [value]
  (->OrientLinkMap value))

(deftype OrientDecimal [value]
  OrientType
  (getDataType [this]
    (byte 21))
  (serialize [this]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          v (bigdec value)
          scale (i/int32 (.scale v))
          serialized-value (serialize v)
          value-size (i/int32 (count serialized-value))]
      (.write dos scale 0 (count scale))
      (.write dos value-size 0 (count value-size))
      (.write dos serialized-value 0 (count serialized-value))
      (.toByteArray bos)))
  (serialize [this offset]
    (serialize this)))

(defn orient-decimal
  [value]
  (->OrientDecimal value))

(defn get-structure
  [record-map]
  (reduce
   (fn [acc k]
     (let [record-map-value (get record-map k)]
       (conj acc {:key-type (getDataType k)
                  :field-name k
                  :position 0
                  :type (getDataType record-map-value)
                  :value record-map-value})))
   []
   (keys record-map)))

(defn header-size
  [headers fixed-header-int]
  (+ 1                                  ; closing header
     (reduce
      (fn [acc k]
        (+ acc (count (serialize k)) fixed-header-int))
      0
      headers)))

(defn serialize-structure-values
  [structure]
  (map
   (fn [s]
     (let [v (:value s)]
       (assoc s :serialized-value (serialize v))))
   structure))

(defn oemap-positions
  [structure offset]
  (let [hsize (header-size
               (map :field-name structure) const/fixed-oemap-header-int)]
    (reduce
     (fn [acc s]
       (if (empty? acc)
         (conj acc
               (assoc s :position (+ offset hsize)))
         (conj acc
               (assoc s :position
                      (+ (count (:serialized-value (last acc)))
                         (:position (last acc)))))))
     []
     structure)))

(defn positions->orient-int32
  [structure]
  (map #(update % :position orient-int32) structure))

(defn oemap->structure
  [data-map offset]
  (-> (get-structure data-map)
      serialize-structure-values
      (oemap-positions offset)
      positions->orient-int32))

(defn serialize-elements
  [header key-order]
  (reduce
   (fn [acc hk]
     (conj acc (serialize (get header hk))))
   []
   key-order))

(defn serialize-headers
  [structure key-order]
  (mapcat
   #(serialize-elements % key-order)
   structure))

(defn write-serialized-data
  [^DataOutputStream dos data]
  (if (= (type data) java.lang.Byte)
    (.writeByte dos data)
    (.write dos data 0 (count data))))

(defn serialize-data
  [structure]
  (->> structure
       (map :serialized-value)))

(deftype OrientEmbeddedMap [value]
  OrientType
  (getDataType [this]
    (byte 12))
  (serialize [this]
    (serialize this 0))
  (serialize [this offset]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (count value)
          size-varint (byte-array (v/varint-unsigned size))
          size-varint-len (count size-varint)
          structure (oemap->structure value offset)
          key-order [:key-type :field-name :position :type]
          serialized-headers (serialize-headers structure key-order)
          serialized-data (serialize-data structure)]
      (.write dos size-varint 0 size-varint-len)
      (doall (map #(write-serialized-data dos %) serialized-headers))
      (doall (map #(write-serialized-data dos %) serialized-data))
      (.toByteArray bos))))

(defn orient-embedded-map
  [value]
  (->OrientEmbeddedMap value))

(defn first-elem
  [record-map offset]
  (let [f (first record-map)
        k (first f)
        v (second f)
        hsize (header-size (keys record-map) const/fixed-header-int)]
    {:key-type (getDataType k)
     :field-name k
     :type (getDataType v)
     :value v
     :serialized-value (serialize v)
     :position (+ 1 offset hsize)}))

(defn rest-elem
  [record-map first-elem]
  (reduce
   (fn [acc [k v]]
     (let [last-elem (last acc)
           pos (+ (count (:serialized-value last-elem))
                  (:position last-elem))]
       (conj
        acc
        {:key-type (getDataType k)
         :field-name k
         :type (getDataType v)
         :value v
         :position pos
         :serialized-value (serialize v pos)})))
   (conj [] first-elem)
   (rest record-map)))

(defn record-map->structure
  [record-map offset]
  (->> (first-elem record-map offset)
       (rest-elem record-map)
       positions->orient-int32))

(deftype OrientEmbedded [value]
  OrientType
  (getDataType [this]
    (byte 9))
  (serialize [this offset]
    (let [bos (ByteArrayOutputStream.)
          dos (DataOutputStream. bos)
          size (count value)
          size-varint (byte-array (v/varint-unsigned size))
          size-varint-len (count size-varint)
          class (first (first value))
          serialized-class (serialize class)
          serialized-class-size (count serialized-class)
          record-map (get value class)
          first-elem-pos (dec (+ offset serialized-class-size))
          structure (record-map->structure record-map first-elem-pos)
          key-order [:field-name :position :type]
          serialized-headers (serialize-headers structure key-order)
          serialized-data (serialize-data structure)]
      (.write dos serialized-class 0 serialized-class-size)
      (doall (map #(write-serialized-data dos %) serialized-headers))
      (.writeByte dos (byte 0))
      (doall (map #(write-serialized-data dos %) serialized-data))
      (.toByteArray bos)))
  (serialize [this]
    (serialize this 0)))

(defn orient-embedded
  [value]
  (->OrientEmbedded value))

(defn serialize-record
  "Serialize `record` for OrientDB.
   `record` must be a Clojure map. It can contain Clojure types (string,
   boolean, etc.) or Orient custom types (OrientLink, OrientBinary, etc.)."
  [record]
  (let [bos (ByteArrayOutputStream.)
        dos (DataOutputStream. bos)
        version (byte 0)
        class (first (first record))
        serialized-class (serialize class)
        serialized-class-size (count serialized-class)
        record-map (get record class)
        structure (record-map->structure record-map serialized-class-size)
        key-order [:field-name :position :type]
        serialized-headers (serialize-headers structure key-order)
        serialized-data (serialize-data structure)]
    (.writeByte dos version)
    (.write dos serialized-class 0 serialized-class-size)
    (doall (map #(write-serialized-data dos %) serialized-headers))
    (.writeByte dos (byte 0))
    (doall (map #(write-serialized-data dos %) serialized-data))
    (.toByteArray bos)))
