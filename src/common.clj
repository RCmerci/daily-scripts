(ns common
  (:require [clojure.instant :refer [read-instant-date]]))

(defn date-string->epoch
  "e.g. \"2012-12-12\" -> 1355270400"
  [date-string]
  (when date-string
    (.getEpochSecond
     (.toInstant
      (read-instant-date date-string)))))

(defn date-string->ms
  "e.g. \"2012-12-12\" -> 1355270400000"
  [date-string]
  (when date-string
    (.toEpochMilli
     (.toInstant
      (read-instant-date date-string)))))
