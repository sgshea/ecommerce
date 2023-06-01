(ns build
  (:require [org.corfield.build :as bb]))

(def lib 'ecommerce.clj)
(def main 'ecommerce.clj.system)

(defn ci "Run the CI pipeline of tests (and build the uberjar)."
  [opts]
  (-> opts
      (assoc :lib lib :main main)
      (bb/clean)
      (bb/uber)))
