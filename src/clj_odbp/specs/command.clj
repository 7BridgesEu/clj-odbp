(ns clj-odbp.specs.command
  (:require [clj-odbp.deserialize.otype :as d]
            [clj-odbp.serialize.otype :as s]))

;; REQUEST_COMMAND > SELECT
(def select-request
  {:operation s/byte-type
   :session-id s/int-type
   :token s/bytes-type
   :mode s/byte-type
   :payload-length s/int-type
   :class-name s/string-type
   :text s/string-type
   :non-text-limit s/int-type
   :fetch-plan s/string-type
   :serialized-params s/string-type})

;; REQUEST_COMMAND > SQL Command
(def command-request
  {:operation s/byte-type
   :session-id s/int-type
   :mode s/byte-type
   :payload-length s/int-type
   :class-name s/string-type
   :text s/string-type
   :has-simple-params s/bool-type
   :simple-params s/bytes-type
   :has-complex-params s/bool-type
   :complex-params s/bytes-type})

;; REQUEST_COMMAND > Script
(def script-request
  {:operation s/byte-type
   :session-id s/int-type
   :mode s/byte-type
   :payload-length s/int-type
   :class-name s/string-type
   :language s/string-type
   :text s/string-type
   :has-simple-params s/bool-type
   :simple-params s/bytes-type
   :has-complex-params s/bool-type
   :complex-params s/bytes-type})

;; REQUEST_COMMAND > Sync response
(def sync-generic-response
  {:session-id d/int-type
   :token d/bytes-type
   :result-type (comp char d/byte-type)})

(def record-response
  {:record-type (comp char d/byte-type)
   :record-cluster d/short-type
   :record-position d/long-type
   :record-version d/int-type
   :record-content d/bytes-type})

;; REQUEST_COMMAND > Async response
(def async-response {})
