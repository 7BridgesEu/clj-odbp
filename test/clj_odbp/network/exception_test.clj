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

(ns clj-odbp.network.exception-test
  (:require [clj-odbp.network.exception :as e]
            [midje.sweet :refer :all])
  (:import [java.io ByteArrayInputStream DataInputStream]))

(defn- provide-input [bytes]
  (->> bytes
       (map byte)
       byte-array
       ByteArrayInputStream.
       DataInputStream.))

(facts
 (fact
  "deserialize-exception should return a vector: [{:class class :message message}]"
  (let [in (let [ex-class (.getBytes "error")
                 ex-message (.getBytes "test")]
             (provide-input (concat [1]
                                    [0 0 0 5]
                                    ex-class
                                    [0 0 0 4]
                                    ex-message
                                    [0])))]
    (e/deserialize-exception in)) => [{:class "error" :message "test"}])
 (fact
  "deserialize-exception should return a vector: [{:class class :message message}]"
  (let [in (let [ex-class1 (.getBytes "error1")
                 ex-message1 (.getBytes "test1")
                 ex-class2 (.getBytes "error2")
                 ex-message2 (.getBytes "test2")]
             (provide-input (concat [1]
                                    [0 0 0 6]
                                    ex-class1
                                    [0 0 0 5]
                                    ex-message1
                                    [1]
                                    [0 0 0 6]
                                    ex-class2
                                    [0 0 0 5]
                                    ex-message2
                                    [0])))]
    (e/deserialize-exception in)) => [{:class "error1" :message "test1"}
                                      {:class "error2" :message "test2"}]))
