(ns clj-odbp.utils
  (:require [clj-odbp
             [net :as net]
             [sessions :as sessions]]
            [clj-odbp.deserialize.exception :as ex])
  (:import [java.io ByteArrayOutputStream DataInputStream DataOutputStream]))

(defn- validate-message
  [spec message]
  (when-not (every?
             #(contains? spec (first %))
             message)
    (throw (Exception. "The message doesn't respect the spec."))))

(defn encode
  [spec message]
  (let [out (ByteArrayOutputStream.)
        stream (DataOutputStream. out)]
    (validate-message spec message)
    (doseq [[field-name value] message
            :let [function (get spec field-name)]]
      (try
        (apply function [stream value])
        (catch Exception e
          (throw (Exception. (str (.getMessage e) " writing " field-name))))))
    out))

(defn decode
  [^DataInputStream in spec]
  (reduce-kv
   (fn [result field-name f]
     (assoc result field-name (f in)))
   {}
   spec))

(defn take-upto
  "Returns a lazy sequence of successive items from coll up to and including
  the first item for which `(pred item)` returns true."
  ([pred]
   (fn [rf]
     (fn
       ([] (rf))
       ([result] (rf result))
       ([result x]
        (let [result (rf result x)]
          (if (pred x)
            (ensure-reduced result)
            result))))))
  ([pred coll]
   (lazy-seq
    (when-let [s (seq coll)]
      (let [x (first s)]
        (cons x (if-not (pred x) (take-upto pred (rest s)))))))))

(defmacro defcommand
  [command-name args request-handler response-handler]
  `(defn ~command-name
     [~@args]
     (try
       (with-open [s# (net/create-socket)]
         (-> s#
             (net/write-request ~request-handler ~@(remove '#{&} args))
             (net/read-response ~response-handler)))
       (catch Exception e#
         (ex/manage-exception {:exception-type (:type (ex-data e#))
                               :exception e#})))))

(defmacro defconnection
  [command-name args request-handler response-handler service]
  `(defn ~command-name
     [~@args]
     (if (sessions/has-session? ~service)
       (sessions/read-session ~service)
       (try
         (with-open [s# (net/create-socket)]
           (-> s#
               (net/write-request ~request-handler ~@(remove '#{&} args))
               (net/read-response ~response-handler)
               (select-keys [:session-id :token])
               (sessions/put-session! ~service))
           (sessions/read-session ~service))
         (catch Exception e#
           (ex/manage-exception {:exception-type (:type (ex-data e#))
                                 :exception e#}))))))
