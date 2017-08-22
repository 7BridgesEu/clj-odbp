;; Copyright 2017 7bridges s.r.l.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clj-odbp.operations.command
  (:require [clj-odbp
             [constants :as constants]
             [utils :refer [encode decode]]]
            [clj-odbp.specs.command :as specs]
            [clj-odbp.serialize.binary.record :as serialize]
            [clj-odbp.deserialize.binary.record :as deserialize]
            [clj-odbp.deserialize.otype :as d])
  (:import java.io.DataInputStream))

(defn get-bytes-type-length [bytes-type]
  (if (empty? bytes-type)
    0
    (+ 4 (count bytes-type))))

(defn get-query-payload-length
  [command fetch-plan serialized-params]
  (+ 4 (count constants/request-command-query)
     4 (count command)
     4                                  ; non-text-limit length
     4 (count fetch-plan)
     (get-bytes-type-length serialized-params)))

(defn serialize-params
  [params]
  (if (empty? params)
    ""
    (let [oem-params (serialize/orient-embedded-map params)]
      (serialize/serialize-record {"params" oem-params}))))

;; REQUEST_COMMAND > SELECT
(defn query-request
  [connection command
   {:keys [params non-text-limit fetch-plan]
    :or {params {} non-text-limit 20 fetch-plan "*:0"}}]
  (let [session-id (:session-id connection)
        token (:token connection)
        serialized-params (serialize-params params)]
    (encode
     specs/query-request
     [[:operation 41]
      [:session-id session-id]
      [:token token]
      [:mode constants/request-command-sync-mode]
      [:payload-length (get-query-payload-length command
                                                 fetch-plan
                                                 serialized-params)]
      [:class-name constants/request-command-query]
      [:text command]
      [:non-text-limit non-text-limit]
      [:fetch-plan fetch-plan]
      [:serialized-params serialized-params]])))

(defn- query-list-response
  [^DataInputStream in]
  (let [list-size (d/int-type in)]
    (reduce
     (fn [acc n]
       (case (d/short-type in)
         0 (conj acc (-> (decode in specs/record-response)
                         deserialize/deserialize-record))
         (conj acc nil)))
     []
     (range list-size))))

(defn query-response
  [^DataInputStream in]
  (let [generic-response (decode in specs/sync-generic-response)
        result-type (:result-type generic-response)]
    (case result-type
      \l (query-list-response in))))
