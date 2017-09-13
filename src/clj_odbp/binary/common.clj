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

(ns clj-odbp.binary.common)

(deftype OrientBinary [value]
  Object
  (equals [this o]
    (if (instance? OrientBinary o)
      (= (.value this) (.value o))
      false)))

(defn orient-binary
  [value]
  {:pre [(vector? value)]}
  (->OrientBinary value))

(deftype OrientORidBag [value]
  Object
  (equals [this o]
    (if (instance? OrientORidBag o)
      (= (.value this) (.value o))
      false)))

(defn orient-orid-bag
  [value]
  (->OrientORidBag value))

(defmulti get-value class)
(defmethod get-value OrientBinary [v]
  (.value v))
(defmethod get-value OrientORidBag [v]
  (.value v))
