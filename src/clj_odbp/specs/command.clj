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

(ns clj-odbp.specs.command
  (:require [clj-odbp.deserialize.otype :as d]
            [clj-odbp.serialize.otype :as s]))

;; REQUEST_COMMAND > SELECT
(def select-request
  {:operation s/byte-type
   :session-id s/int-type
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
(def sync-response
  {:session-id d/int-type})

;; REQUEST_COMMAND > Async response
(def async-response {})
