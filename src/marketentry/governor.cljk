(ns marketentry.governor
  "Market-Entry Compliance Governor -- the independent compliance layer
  that earns the MarketEntry-LLM the right to commit. The LLM has no
  notion of Chadian procurement law, whether a claimed engagement fee
  actually equals base + months x rate, whether the engagement's own
  declared nationality/ownership/gender facts actually qualify it for
  the Art.80 (préférence nationale) or Art.81 (préférence genre) price
  preference it claims under the Code des Marchés Publics (Décret
  N°2130/PR/2020), whether a DGI (Direction Générale des Impôts) tax
  record has been verified for a filing that requires it, whether a
  bidder's own declared conflict-of-interest fact should exclude it
  under Art.128(1)(e), or when a draft stops being a draft and becomes
  a real-world armp-tchad.com submission, so this MUST be a separate
  system able to *reject* a proposal and fall back to HOLD.

  `:itonami.blueprint/governor` is `:market-entry-compliance-governor`
  (shared family keyword on blueprints).

  Seven checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them. The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `marketentry.phase`: for `:stake
  :actuation/draft-filing`/`:actuation/submit-filing` NO phase ever
  allows auto-commit either. Two independent layers agree that
  actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source
                                       (`marketentry.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:filing/draft`/
                                       `:filing/submit`, has the
                                       jurisdiction actually been
                                       assessed with a full evidence
                                       checklist on file?
    3. Preference-regime           -- for `:filing/submit`, when the
       ineligible                     engagement declares a
                                       `:preference-claim` (`:national`
                                       or `:gender`), INDEPENDENTLY
                                       recompute whether the
                                       engagement's own declared
                                       nationality/ownership/gender
                                       facts satisfy that regime's OWN
                                       eligibility test AND whether its
                                       claimed price stays within that
                                       regime's OWN percentage ceiling
                                       relative to the best bid, and
                                       HARD-hold if not. FLAGSHIP
                                       genuinely new check for the
                                       iso3166 family (grep-verified
                                       absent as a governor check
                                       function/rule name fleet-wide at
                                       build time) -- a TWO-REGIME
                                       DISPATCH with a COMPARATIVE
                                       price-ceiling-band test, a check
                                       SHAPE genuinely different from
                                       every prior sibling's (see
                                       `marketentry.registry`'s
                                       docstring for the full
                                       differentiation).
    4. Engagement fee mismatch     -- for `:filing/submit`,
                                       INDEPENDENTLY recompute whether
                                       the engagement's own `:claimed-
                                       fee` equals `base-fee +
                                       monthly-rate x monitoring-
                                       months` -- honest reapplication
                                       of the ground-truth-recompute
                                       discipline sibling actors use.
    5. Procurement-official         -- for `:filing/submit`,
       conflict of interest            INDEPENDENTLY check the
                                       engagement's own declared
                                       `:procurement-official-conflict?`
                                       fact. Grounded in Décret
                                       N°2130/PR/2020, Art.128(1)(e)
                                       (see `marketentry.facts`).
    6. DGI tax record unverified    -- for `:filing/submit`, when the
                                       engagement declares
                                       `:requires-dgi-record? true`,
                                       INDEPENDENTLY check
                                       `:dgi-record-verified?`.
                                       CONDITIONAL on the engagement's
                                       own ground truth. Grounded in
                                       the Direction Générale des
                                       Impôts (DGI), Ministère des
                                       Finances et du Budget (see
                                       `marketentry.facts`).
    7. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:filing/draft`/
                                       `:filing/submit` (REAL acts)
                                       -> escalate.

  Two more guards, double-draft/double-submit prevention, are enforced
  off dedicated `:drafted?`/`:submitted?` facts (never a `:status`
  value)."
  (:require [marketentry.facts :as facts]
            [marketentry.registry :as registry]
            [marketentry.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real portal package and submitting a real portal
  registration are the two real-world actuation events this actor
  performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's market-entry requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:filing/draft`/`:filing/submit`, the jurisdiction's required
  registration evidence must actually be satisfied."
  [{:keys [op subject]} st]
  (when (contains? #{:filing/draft :filing/submit} op)
    (let [e (store/engagement st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(non-faillite/domiciliation bancaire/caution de soumission/ARMP非排除証明/CNPS/patente/quitus fiscal/RCCM等)が充足していない状態での提案"}]))))

(defn- preference-regime-ineligible-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own declared nationality/ownership/gender facts satisfy
  the CLAIMED regime's (Art.80 national OR Art.81 gender) own
  eligibility test AND price ceiling -- the flagship check this
  vertical adds. HARD-hold when the engagement declares a
  `:preference-claim` that is not independently entitled to it."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (registry/preference-regime-ineligible-claim? e)
        [{:rule :preference-regime-ineligible
          :detail (str subject " は優遇措置区分 " (:preference-claim e)
                      " (Décret N°2130/PR/2020 Art.80国内優遇/Art.81ジェンダー優遇) を主張しているが、"
                      "独立再計算による適格性または価格上限(15%/10%)を満たさない")}]))))

(defn- engagement-fee-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own claimed fee equals base + months x rate."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (registry/engagement-fee-matches-claim? e)
        [{:rule :engagement-fee-mismatch
          :detail (str subject " の申告手数料(" (:claimed-fee e)
                      ")が独立再計算値(" (registry/compute-engagement-fee e) ")と一致しない")}]))))

(defn- procurement-official-conflict-violations
  "For `:filing/submit`, INDEPENDENTLY check the engagement's own
  declared `:procurement-official-conflict?` fact. Grounded in Décret
  N°2130/PR/2020, Art.128(1)(e): a bidder is excluded when a
  procurement official/commission member/control-organ member/approval
  authority holds a financial or personal interest in it."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (true? (:procurement-official-conflict? e))
        [{:rule :procurement-official-conflict-of-interest
          :detail (str subject " はArt.128(1)(e)に基づき、調達担当者/委員会/統制機関/承認権限者との"
                      "利益相反が申告されており、入札資格を欠く")}]))))

(defn- dgi-record-unverified-violations
  "For `:filing/submit`, when the engagement declares
  `:requires-dgi-record? true`, INDEPENDENTLY check
  `:dgi-record-verified?` -- CONDITIONAL on the engagement's own
  ground truth. Grounded in the Direction Générale des Impôts (DGI),
  Ministère des Finances et du Budget."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:requires-dgi-record? e))
                 (not (true? (:dgi-record-verified? e))))
        [{:rule :dgi-record-unverified
          :detail (str subject " はDGI(Direction Générale des Impôts)税務記録(quitus fiscal)の確認を要するが未確認 -- 提出提案は進められない")}]))))

(defn- already-drafted-violations
  "For `:filing/draft`, refuses to draft the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/draft)
    (when (store/engagement-already-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "For `:filing/submit`, refuses to submit the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (when (store/engagement-already-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors a MarketEntry-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (preference-regime-ineligible-violations request st)
                           (engagement-fee-mismatch-violations request st)
                           (procurement-official-conflict-violations request st)
                           (dgi-record-unverified-violations request st)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
