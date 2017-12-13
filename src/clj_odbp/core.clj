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

(ns clj-odbp.core
  (:require [clj-odbp.network.socket :as s]
            [clj-odbp.operations.command :as command]
            [clj-odbp.operations.db :as db]
            [clj-odbp.operations.record :as record]
            [clj-odbp.session :refer [defcommand defsession]]))

(defsession connect-server
  [connection-params username password]
  db/connect-request
  db/connect-response)

(defcommand shutdown-server
  [username password]
  db/shutdown-request
  db/shutdown-response)

(defsession db-open
  [connection-parameters db-name username password]
  db/db-open-request
  db/db-open-response)

(defcommand db-create
  [session db-name & opts]
  db/db-create-request
  db/db-create-response)

(defn db-close
  []
  (with-open [socket (s/create-socket)]
    (-> socket
        (s/write-request db/db-close-request))
    {}))

(defcommand db-exist
  [session db-name]
  db/db-exist-request
  db/db-exist-response)

(defcommand db-drop
  [session db-name]
  db/db-drop-request
  db/db-drop-response)

(defcommand db-size
  [session]
  db/db-size-request
  db/db-size-response)

(defcommand db-countrecords
  [session]
  db/db-countrecords-request
  db/db-countrecords-response)

(defcommand db-reload
  [session]
  db/db-reload-request
  db/db-reload-response)

(defcommand record-load
  [session rid]
  record/record-load-request
  record/record-load-response)

(defcommand record-create
  [session record-content]
  record/record-create-request
  record/record-create-response)

(defcommand record-update
  [session rid record-content]
  record/record-update-request
  record/record-update-response)

(defcommand record-delete
  [session rid]
  record/record-delete-request
  record/record-delete-response)

(defcommand query-command
  [session query & opts]
  command/query-request
  command/query-response)

(defcommand execute-command
  [session command & opts]
  command/execute-request
  command/query-response)

(defcommand execute-script
  [session command language & opts]
  command/script-request
  command/query-response)
