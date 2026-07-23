(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest tcd-has-spec-basis
  (let [sb (facts/spec-basis "TCD")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (some? (facts/corporate-number-spec-basis "TCD")))
    (is (some? (facts/preference-national-spec-basis "TCD")))
    (is (some? (facts/preference-gender-spec-basis "TCD")))))

(deftest tcd-rep-spec-basis-is-populated
  (testing "unlike CAF's honest-nil, this iteration independently confirmed Décret N°2130/PR/2020's own Art.128(1)(e)"
    (is (some? (facts/rep-spec-basis "TCD")))
    (is (string? (:rep-legal-basis (facts/rep-spec-basis "TCD"))))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "TCD")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "TCD" all)))
    (is (not (facts/required-evidence-satisfied? "TCD" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["TCD" "USA" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 2 (:covered c)))
    (is (= ["ATL"] (:missing-jurisdictions c)))))

(deftest preference-national-spec-basis-criteria
  (let [pn (facts/preference-national-spec-basis "TCD")]
    (is (= 0.15 (get-in pn [:preference-national-criteria :pct-ceiling])))
    (is (contains? (get-in pn [:preference-national-criteria :eligible-tests])
                   :chadian-artisan-or-entrepreneur))
    (is (contains? (get-in pn [:preference-national-criteria :eligible-tests])
                   :chadian-capital-majority-direct))
    (is (contains? (get-in pn [:preference-national-criteria :eligible-tests])
                   :chadian-capital-majority-indirect))))

(deftest preference-gender-spec-basis-criteria
  (let [pg (facts/preference-gender-spec-basis "TCD")]
    (is (= 0.10 (get-in pg [:preference-gender-criteria :pct-ceiling])))
    (is (contains? (get-in pg [:preference-gender-criteria :eligible-tests])
                   :chadian-women-business))))

(deftest preference-regimes-are-textually-distinct
  (testing "Art.80 (national, 15%) and Art.81 (gender, 10%) are two SEPARATE regimes, not tiers of one"
    (let [pn (facts/preference-national-spec-basis "TCD")
          pg (facts/preference-gender-spec-basis "TCD")]
      (is (not= (get-in pn [:preference-national-criteria :pct-ceiling])
                (get-in pg [:preference-gender-criteria :pct-ceiling])))
      (is (not= (:preference-national-legal-basis pn) (:preference-gender-legal-basis pg))))))
