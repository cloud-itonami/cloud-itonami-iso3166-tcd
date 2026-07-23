(ns marketentry.store
  "SSoT for the Chad (TCD) market-entry compliance actor, behind a
  `Store` protocol so the backend is a swap, not a rewrite -- the same
  seam every prior cloud-itonami actor in this fleet uses.

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store.

  Both implement the same protocol and pass the same contract
  (test/marketentry/store_contract_test.clj).

  The primary entity here is an `engagement` -- filing-draft and
  filing-submit actuation events apply SEQUENTIALLY to the SAME
  engagement record (draft first, submit later). Dedicated
  double-actuation-guard booleans (`:drafted?`/`:submitted?`, never a
  `:status` value).

  The ledger stays append-only on every backend."
  (:require #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [marketentry.registry :as registry]
            [langchain.db :as d]))

(defprotocol Store
  (engagement [s id])
  (all-engagements [s])
  (assessment-of [s engagement-id] "committed jurisdiction assessment, or nil")
  (ledger [s])
  (draft-history [s] "the append-only filing-draft history")
  (submit-history [s] "the append-only filing-submit history")
  (next-draft-sequence [s jurisdiction])
  (next-submit-sequence [s jurisdiction])
  (engagement-already-drafted? [s engagement-id])
  (engagement-already-submitted? [s engagement-id])
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-engagements [s engagements] "replace/seed the engagement directory"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained engagement set covering both actuation
  lifecycles (draft, submit) plus the governor's own new checks.
  `:preference-claim` / `:chadian-artisan-or-entrepreneur?` /
  `:chadian-capital-majority-direct?` /
  `:chadian-capital-majority-indirect?` / `:chadian-women-business?` /
  `:best-bid-price` / `:claimed-preferential-price` are ground truth
  for the `preference-regime-ineligible` flagship check (Décret
  N°2130/PR/2020, Art.80 préférence nationale / Art.81 préférence
  genre); `:procurement-official-conflict?` is ground truth for the
  Art.128(1)(e) conflict-of-interest check; `:requires-dgi-record?` /
  `:dgi-record-verified?` are ground truth for the conditional DGI tax
  check."
  []
  {:engagements
   {"eng-1" {:id "eng-1" :operator "N'Djamena Fabrication SARL" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim nil
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price nil
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}
    "eng-2" {:id "eng-2" :operator "Atlantis LLC" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim nil
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price nil
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "ATL" :status :intake}
    "eng-3" {:id "eng-3" :operator "Chari Systems SA" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 999000.0
             :preference-claim nil
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price nil
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}
    "eng-4" {:id "eng-4" :operator "Sahel Artisanat Groupement" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim :national
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price 1100000.0
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}
    "eng-8" {:id "eng-8" :operator "Mayo-Kebbi Batiment SA" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim :national
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? true
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price 1200000.0
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}
    "eng-5" {:id "eng-5" :operator "Logone Logistics SARL" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim nil
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price nil
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? false
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}
    "eng-6" {:id "eng-6" :operator "Femmes d'Affaires du Ouaddaï Groupement" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim :gender
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? true
             :best-bid-price 1000000.0 :claimed-preferential-price 1090000.0
             :procurement-official-conflict? false
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}
    "eng-7" {:id "eng-7" :operator "Kanem Travaux SA" :portal "armp-tchad.com"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :preference-claim nil
             :chadian-artisan-or-entrepreneur? false
             :chadian-capital-majority-direct? false
             :chadian-capital-majority-indirect? false
             :chadian-women-business? false
             :best-bid-price 1000000.0 :claimed-preferential-price nil
             :procurement-official-conflict? true
             :requires-dgi-record? true :dgi-record-verified? true
             :drafted? false :submitted? false
             :jurisdiction "TCD" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- draft-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-draft-sequence s (:jurisdiction e))
        result (registry/register-draft engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:drafted? true
                        :draft-number (get result "draft_number")}}))

(defn- submit-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-submit-sequence s (:jurisdiction e))
        result (registry/register-submit engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:submitted? true
                        :submit-number (get result "submit_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (engagement [_ id] (get-in @a [:engagements id]))
  (all-engagements [_] (sort-by :id (vals (:engagements @a))))
  (assessment-of [_ engagement-id] (get-in @a [:assessments engagement-id]))
  (ledger [_] (:ledger @a))
  (draft-history [_] (:draft-records @a))
  (submit-history [_] (:submit-records @a))
  (next-draft-sequence [_ jurisdiction] (get-in @a [:draft-sequences jurisdiction] 0))
  (next-submit-sequence [_ jurisdiction] (get-in @a [:submit-sequences jurisdiction] 0))
  (engagement-already-drafted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :drafted?])))
  (engagement-already-submitted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :submitted?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (swap! a update-in [:engagements (:id value)] merge value)

      :assessment/set
      (swap! a assoc-in [:assessments (first path)] payload)

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:draft-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :draft-records registry/append result))))
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:submit-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :submit-records registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-engagements [s engagements] (when (seq engagements) (swap! a assoc :engagements engagements)) s))

(defn seed-db
  "A MemStore seeded with the demo engagement set."
  []
  (->MemStore (atom (assoc (demo-data)
                           :assessments {}
                           :ledger [] :draft-sequences {} :draft-records []
                           :submit-sequences {} :submit-records []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  {:engagement/id                   {:db/unique :db.unique/identity}
   :assessment/engagement-id        {:db/unique :db.unique/identity}
   :ledger/seq                      {:db/unique :db.unique/identity}
   :draft-record/seq                {:db/unique :db.unique/identity}
   :submit-record/seq               {:db/unique :db.unique/identity}
   :draft-sequence/jurisdiction     {:db/unique :db.unique/identity}
   :submit-sequence/jurisdiction    {:db/unique :db.unique/identity}})

(defn- enc [v] (pr-str v))
(defn- dec* [s] (when s (edn/read-string s)))

(defn- engagement->tx [{:keys [id operator portal base-fee monthly-rate monitoring-months claimed-fee
                               preference-claim
                               chadian-artisan-or-entrepreneur?
                               chadian-capital-majority-direct?
                               chadian-capital-majority-indirect?
                               chadian-women-business?
                               best-bid-price claimed-preferential-price
                               procurement-official-conflict?
                               requires-dgi-record? dgi-record-verified?
                               drafted? submitted?
                               jurisdiction status draft-number submit-number]}]
  (cond-> {:engagement/id id}
    operator                              (assoc :engagement/operator operator)
    portal                                (assoc :engagement/portal portal)
    base-fee                              (assoc :engagement/base-fee base-fee)
    monthly-rate                          (assoc :engagement/monthly-rate monthly-rate)
    monitoring-months                     (assoc :engagement/monitoring-months monitoring-months)
    claimed-fee                           (assoc :engagement/claimed-fee claimed-fee)
    preference-claim                      (assoc :engagement/preference-claim preference-claim)
    (some? chadian-artisan-or-entrepreneur?)   (assoc :engagement/chadian-artisan-or-entrepreneur? chadian-artisan-or-entrepreneur?)
    (some? chadian-capital-majority-direct?)   (assoc :engagement/chadian-capital-majority-direct? chadian-capital-majority-direct?)
    (some? chadian-capital-majority-indirect?) (assoc :engagement/chadian-capital-majority-indirect? chadian-capital-majority-indirect?)
    (some? chadian-women-business?)            (assoc :engagement/chadian-women-business? chadian-women-business?)
    (some? best-bid-price)                (assoc :engagement/best-bid-price best-bid-price)
    (some? claimed-preferential-price)    (assoc :engagement/claimed-preferential-price claimed-preferential-price)
    (some? procurement-official-conflict?) (assoc :engagement/procurement-official-conflict? procurement-official-conflict?)
    (some? requires-dgi-record?)          (assoc :engagement/requires-dgi-record? requires-dgi-record?)
    (some? dgi-record-verified?)          (assoc :engagement/dgi-record-verified? dgi-record-verified?)
    (some? drafted?)                      (assoc :engagement/drafted? drafted?)
    (some? submitted?)                    (assoc :engagement/submitted? submitted?)
    jurisdiction                          (assoc :engagement/jurisdiction jurisdiction)
    status                                (assoc :engagement/status status)
    draft-number                          (assoc :engagement/draft-number draft-number)
    submit-number                         (assoc :engagement/submit-number submit-number)))

(def ^:private engagement-pull
  [:engagement/id :engagement/operator :engagement/portal :engagement/base-fee :engagement/monthly-rate
   :engagement/monitoring-months :engagement/claimed-fee
   :engagement/preference-claim
   :engagement/chadian-artisan-or-entrepreneur?
   :engagement/chadian-capital-majority-direct?
   :engagement/chadian-capital-majority-indirect?
   :engagement/chadian-women-business?
   :engagement/best-bid-price :engagement/claimed-preferential-price
   :engagement/procurement-official-conflict?
   :engagement/requires-dgi-record? :engagement/dgi-record-verified?
   :engagement/drafted? :engagement/submitted?
   :engagement/jurisdiction :engagement/status :engagement/draft-number :engagement/submit-number])

(defn- pull->engagement [m]
  (when (:engagement/id m)
    {:id (:engagement/id m) :operator (:engagement/operator m) :portal (:engagement/portal m)
     :base-fee (:engagement/base-fee m) :monthly-rate (:engagement/monthly-rate m)
     :monitoring-months (:engagement/monitoring-months m) :claimed-fee (:engagement/claimed-fee m)
     :preference-claim (:engagement/preference-claim m)
     :chadian-artisan-or-entrepreneur? (boolean (:engagement/chadian-artisan-or-entrepreneur? m))
     :chadian-capital-majority-direct? (boolean (:engagement/chadian-capital-majority-direct? m))
     :chadian-capital-majority-indirect? (boolean (:engagement/chadian-capital-majority-indirect? m))
     :chadian-women-business? (boolean (:engagement/chadian-women-business? m))
     :best-bid-price (:engagement/best-bid-price m)
     :claimed-preferential-price (:engagement/claimed-preferential-price m)
     :procurement-official-conflict? (boolean (:engagement/procurement-official-conflict? m))
     :requires-dgi-record? (boolean (:engagement/requires-dgi-record? m))
     :dgi-record-verified? (boolean (:engagement/dgi-record-verified? m))
     :drafted? (boolean (:engagement/drafted? m)) :submitted? (boolean (:engagement/submitted? m))
     :jurisdiction (:engagement/jurisdiction m) :status (:engagement/status m)
     :draft-number (:engagement/draft-number m) :submit-number (:engagement/submit-number m)}))

(defrecord DatomicStore [conn]
  Store
  (engagement [_ id]
    (pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id id])))
  (all-engagements [_]
    (->> (d/q '[:find [?id ...] :where [?e :engagement/id ?id]] (d/db conn))
         (map #(pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id %])))
         (sort-by :id)))
  (assessment-of [_ engagement-id]
    (dec* (d/q '[:find ?p . :in $ ?eid
                :where [?a :assessment/engagement-id ?eid] [?a :assessment/payload ?p]]
              (d/db conn) engagement-id)))
  (ledger [_]
    (->> (d/q '[:find ?s ?f :where [?e :ledger/seq ?s] [?e :ledger/fact ?f]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (draft-history [_]
    (->> (d/q '[:find ?s ?r :where [?e :draft-record/seq ?s] [?e :draft-record/record ?r]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (submit-history [_]
    (->> (d/q '[:find ?s ?r :where [?e :submit-record/seq ?s] [?e :submit-record/record ?r]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (next-draft-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :draft-sequence/jurisdiction ?j] [?e :draft-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (next-submit-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :submit-sequence/jurisdiction ?j] [?e :submit-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (engagement-already-drafted? [s engagement-id]
    (boolean (:drafted? (engagement s engagement-id))))
  (engagement-already-submitted? [s engagement-id]
    (boolean (:submitted? (engagement s engagement-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (d/transact! conn [(engagement->tx value)])

      :assessment/set
      (d/transact! conn [{:assessment/engagement-id (first path) :assessment/payload (enc payload)}])

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-draft-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:draft-sequence/jurisdiction jurisdiction :draft-sequence/next next-n}
                      {:draft-record/seq (count (draft-history s)) :draft-record/record (enc (get result "record"))}])
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-submit-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:submit-sequence/jurisdiction jurisdiction :submit-sequence/next next-n}
                      {:submit-record/seq (count (submit-history s)) :submit-record/record (enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (d/transact! conn [{:ledger/seq (count (ledger s)) :ledger/fact (enc fact)}])
    fact)
  (with-engagements [s engagements]
    (when (seq engagements) (d/transact! conn (mapv engagement->tx (vals engagements)))) s))

(defn datomic-store
  ([] (datomic-store {}))
  ([{:keys [engagements]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-engagements s engagements))))

(defn datomic-seed-db
  []
  (datomic-store (demo-data)))
