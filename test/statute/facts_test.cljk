(ns statute.facts-test
  (:require [kotoba.lang.text :as str]
            [clojure.test :refer [deftest is]]
            [statute.facts :as facts]))

(deftest tcd-has-spec-basis
  (let [sb (facts/spec-basis "TCD")]
    (is (= 3 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "https://") sb))
    (is (every? :statute/law-number sb))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["TCD" "JPN" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "JPN"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["tcd.ohada-auscgie"]
         (mapv :statute/id (facts/by-topic "TCD" :corporate-governance))))
  (is (= ["tcd.code-du-travail-1996"]
         (mapv :statute/id (facts/by-topic "TCD" :labor))))
  (is (= ["tcd.charte-investissements-2008"]
         (mapv :statute/id (facts/by-topic "TCD" :investment))))
  (is (empty? (facts/by-topic "ATL" :labor))))
