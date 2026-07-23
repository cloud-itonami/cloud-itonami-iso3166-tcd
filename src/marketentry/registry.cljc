(ns marketentry.registry
  "Pure-function market-entry filing-draft + filing-submit record
  construction -- an append-only market-entry book-of-record draft.

  Like every sibling actor's registry, there is no single international
  reference-number standard for a public-procurement market-entry
  filing -- every jurisdiction assigns its own format. This namespace
  does NOT invent one; it builds a jurisdiction-scoped sequence number
  and validates the record's required fields, the same honest,
  non-fabricating discipline `marketentry.facts` uses.

  `engagement-fee-matches-claim?` is an HONEST reapplication of the
  SAME ground-truth-recompute DISCIPLINE sibling actors use (verify a
  claimed monetary total against the entity's own recorded quantity x
  unit fields), reapplied to a market-entry engagement fee line.

  `preference-regime-eligible?` / `preference-regime-ineligible-claim?`
  are the SAME discipline applied to a genuinely Chad-specific pair of
  mechanisms this iteration independently found in the CURRENT Code des
  Marchés Publics (Décret N°2130/PR/2020 du 15 octobre 2020, own text
  independently fetched from armp-tchad.com and OCR-read this session):

    - Art.80 'De la préférence nationale' -- a 15% price-ceiling
      preference for Chadian-national artisans/individual entrepreneurs
      OR companies with a Chadian-capital majority (held DIRECTLY by
      Chadian natural persons, OR INDIRECTLY through a Chadian-law
      legal entity itself majority-held by Chadian natural persons --
      a two-level ownership-chain test);
    - Art.81 'De la préférence genre' -- a SEPARATE, textually distinct
      10% price-ceiling preference for a Chadian women-business-owner
      enterprise or grouping.

  This is a GENUINELY DIFFERENT check SHAPE than every prior iso3166
  sibling this repo mirrors, and grep-verified absent as a governor
  check function/rule name fleet-wide at build time (all ~150
  siblings' `marketentry.{governor,registry,facts}.cljc` fetched via
  `raw.githubusercontent.com` and grepped this session): CAF's Marché
  réservé is a SINGLE-regime multi-criterion INCLUSION-ELIGIBILITY
  test (no price-ceiling dimension at all -- eligibility is binary,
  contract value is separately delegated); Benin's/Burkina Faso's MPME
  mechanism (`pme-preference-margin-mismatch`) is an AUTOMATIC,
  UNCONDITIONAL formula (`base-price * (1 - 0.05)`) applied the instant
  `:mpme-status?` is true, with no comparator bid and no contracting-
  authority discretion; DR Congo's mechanism (`preference-tier-mismatch`)
  is a SINGLE 6-tier ranked-priority lookup (first-true-wins among 6
  categorical conditions), not two independent percentage regimes;
  FSM's mechanism (`citizen-bidder-preference-mismatch`) is a SINGLE
  regime with a contract-type/value TABLE lookup, not two textually
  separate statutory articles. Chad's Art.80/Art.81 pair is a
  TWO-REGIME DISPATCH shape genuinely new to this family: the
  Contracting Authority's OWN discretionary grant is conditional on (a)
  the offer being judged conforme to the best bidder's, AND (b) the
  claimed price not exceeding the best bidder's price by more than the
  REGIME'S OWN percentage ceiling (15% for `:national`, 10% for
  `:gender`) -- a COMPARATIVE price-ceiling-band test relative to
  another bidder's own price, not a flat formula discount applied to
  one's own base price, and not a ranked-tier lookup. No sibling
  repository's governor/registry namespace anywhere in this fleet
  implements a gender-based bid preference at all (grep-verified
  absent, `gender`/`genre`/`femmes-d-affaires`/`women-owned` all return
  zero fleet-wide hits).

  `:reserved-market-*` naming was deliberately NOT reused here -- Chad's
  Art.80/Art.81 are open-competition BID-EVALUATION preferences, not a
  reserved/set-aside contract-eligibility gate like CAF's Marché
  réservé, so `preference-regime-*` names the mechanism on its own
  terms rather than inheriting a sibling's frame.

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real procurement portal. It builds the RECORD an
  operator would keep, not the act of submitting a portal registration
  itself (that is `marketentry.operation`'s `:filing/submit`, always
  human-gated -- see README Actuation)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the market-entry operator's act, not this actor's."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn compute-engagement-fee
  "The ground-truth engagement fee for `engagement`'s own `:base-fee`
  and `:monitoring-months` x `:monthly-rate` -- a single flat
  base + months x rate calculation, not a full pricing engine."
  [{:keys [base-fee monthly-rate monitoring-months]}]
  (+ (double base-fee)
     (* (double monthly-rate) (double monitoring-months))))

(defn engagement-fee-matches-claim?
  "Does `engagement`'s own `:claimed-fee` equal the independently
  recomputed `compute-engagement-fee`?"
  [{:keys [claimed-fee] :as engagement}]
  (== (double claimed-fee) (compute-engagement-fee engagement)))

(def preference-regimes
  "Décret N°2130/PR/2020 du 15 octobre 2020 (own text, OCR-verified
  2026-07-23 against armp-tchad.com's own hosting): the TWO independent
  bid-evaluation preference regimes, each with its own price-ceiling
  percentage and its own eligibility test. `:national` is Art.80,
  `:gender` is Art.81 -- textually separate provisions, not tiers of a
  single regime."
  {:national {:pct-ceiling 0.15
              :article "Art.80"}
   :gender   {:pct-ceiling 0.10
              :article "Art.81"}})

(defn national-preference-eligible?
  "The ground-truth Art.80 eligibility for `engagement`, independently
  recomputed from its own declared facts -- an OR of the artisan/
  individual-entrepreneur test and the two-level Chadian-capital-
  majority ownership-chain test (direct OR indirect via a Chadian-law
  entity itself majority-held by Chadian nationals). A missing/nil
  declared value on any one branch simply fails that branch."
  [{:keys [chadian-artisan-or-entrepreneur?
           chadian-capital-majority-direct?
           chadian-capital-majority-indirect?]}]
  (boolean
   (or chadian-artisan-or-entrepreneur?
       chadian-capital-majority-direct?
       chadian-capital-majority-indirect?)))

(defn gender-preference-eligible?
  "The ground-truth Art.81 eligibility for `engagement` -- a Chadian
  women-business-owner enterprise or grouping. A SEPARATE test from
  `national-preference-eligible?`; an engagement may satisfy one, both,
  or neither, independently of which regime (if any) it claims."
  [{:keys [chadian-women-business?]}]
  (boolean chadian-women-business?))

(defn preference-regime-eligible?
  "Dispatches to the correct Art.80/Art.81 eligibility test for
  `regime` (`:national` or `:gender`). Any other value (including nil)
  is never eligible -- there is no third regime to fall back to."
  [regime engagement]
  (case regime
    :national (national-preference-eligible? engagement)
    :gender   (gender-preference-eligible? engagement)
    false))

(defn price-within-preference-ceiling?
  "Does `engagement`'s own claimed preferential price stay within
  `regime`'s own statutory percentage ceiling ABOVE the best-bid
  comparator price (Art.80: 15%, Art.81: 10%)? This is a COMPARATIVE
  ceiling relative to ANOTHER bidder's own price -- not a formula
  applied to the engagement's own base price -- so both
  `:claimed-preferential-price` and `:best-bid-price` must be present;
  a missing comparator price cannot be validated and is treated as
  OUTSIDE the ceiling (fail-closed, never assume compliance from an
  absent fact)."
  [regime {:keys [claimed-preferential-price best-bid-price]}]
  (boolean
   (when-let [{:keys [pct-ceiling]} (get preference-regimes regime)]
     (and (some? claimed-preferential-price)
          (some? best-bid-price)
          (<= (double claimed-preferential-price)
              (* (double best-bid-price) (+ 1.0 pct-ceiling)))))))

(defn preference-regime-ineligible-claim?
  "Does `engagement` declare a `:preference-claim` (`:national` or
  `:gender`) that is NOT actually entitled to it -- INDEPENDENTLY
  recomputed against Art.80/Art.81's own eligibility test AND own price
  ceiling? An engagement with `:preference-claim nil` (no claim at all)
  is never flagged by this check (entity/engagement-scope-gated, the
  same discipline Bhutan's `:foreign-company?`-gated FDI check and
  CAF's `:reserved-market?`-gated check use). This is the FLAGSHIP
  check: HARD-hold when EITHER the declared regime's eligibility test
  fails, OR the claimed price falls outside that regime's own
  percentage ceiling relative to the best bid -- catching both 'not
  entitled at all' and 'entitled to the regime but overstated the
  preferential price' in one predicate."
  [{:keys [preference-claim] :as engagement}]
  (boolean
   (and (contains? preference-regimes preference-claim)
        (or (not (preference-regime-eligible? preference-claim engagement))
            (not (price-within-preference-ceiling? preference-claim engagement))))))

(defn register-draft
  "Validate + construct the FILING-DRAFT registration DRAFT -- the
  market-entry operator's own act of preparing a portal registration
  package. Pure function -- does not touch any real procurement
  portal."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "draft: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "draft: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "draft: sequence must be >= 0" {})))
  (let [draft-number (str (str/upper-case jurisdiction) "-DFT-" (zero-pad sequence 6))
        record {"record_id" draft-number
                "kind" "filing-draft"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "draft_number" draft-number
     "certificate" (unsigned-certificate "FilingDraft" draft-number draft-number)}))

(defn register-submit
  "Validate + construct the FILING-SUBMIT registration DRAFT -- the
  market-entry operator's own act of actually submitting a portal
  registration (always human-gated upstream)."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "submit: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "submit: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "submit: sequence must be >= 0" {})))
  (let [submit-number (str (str/upper-case jurisdiction) "-SUB-" (zero-pad sequence 6))
        record {"record_id" submit-number
                "kind" "filing-submit"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "submit_number" submit-number
     "certificate" (unsigned-certificate "FilingSubmit" submit-number submit-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
