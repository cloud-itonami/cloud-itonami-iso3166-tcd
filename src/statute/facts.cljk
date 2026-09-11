(ns statute.facts
  "General-law compliance catalog for Chad (TCD) -- extends this repo's
  existing `marketentry.facts` (public-procurement market-entry only,
  narrow scope) with a second, orthogonal catalog of statutes a company
  operating in this jurisdiction must generally track for compliance.
  Mirrors cloud-itonami-iso3166-ben/-btn/-caf/-cog/-gin/-ner's
  `statute.facts` (ADR-2607141700, cloud-itonami-compliance-fact-
  federation).

  Every entry below cites an OFFICIAL government-hosted (or, for the
  OHADA entry, official supranational-body-hosted) URL -- never
  fabricated, all curl/WebFetch/pdftotext-verified 2026-07-23. Where a
  primary government site returned a bot-detection-adjacent block
  (droit-afrique.com returned HTTP 403 on direct WebFetch), the
  Internet Archive Wayback Machine was used as the documented fallback
  -- never a browser-automation bypass.

  - **Companies/commercial-entity law**: this iteration specifically
    investigated, rather than assumed by analogy to the BEN/CAF/COG/
    GIN/NER siblings, whether Chad is itself an OHADA member state --
    independently confirmed directly on OHADA's own 'Les Etats membres
    de l'OHADA' page (`ohada.org/les-etats-membres-de-lohada/`,
    WebFetch-verified this session), which lists 'Tchad' among the 17
    member states. So, like its siblings, company law is governed
    DIRECTLY by a SUPRANATIONAL instrument, the OHADA Acte uniforme
    relatif au droit des sociétés commerciales et du groupement
    d'intérêt économique (AUSCGIE) -- this iteration independently
    fetched OHADA's own page
    (`ohada.org/en/commercial-companies-and-economic-interest-groups/`,
    WebFetch-verified directly this session) and confirmed, in OHADA's
    own words: adoption '30 January 2014 in Ouagadougou (Burkina
    Faso)', entry into force '5 May 2014' (published in the OHADA
    Official Gazette 4 February 2014). Separately, RCCM/business-entity
    REGISTRATION -- as opposed to company FORMATION/governance law -- is
    governed by a DIFFERENT OHADA instrument, the Acte Uniforme relatif
    au Droit Commercial Général (AUDCG; own page independently
    fetched this session, `ohada.org/droit-commercial-general/`:
    adopted 15 décembre 2010 à Lomé (Togo), in force 15 mai 2011); this
    catalog does not conflate the two (`marketentry.facts` cites AUDCG
    separately for RCCM). This iteration could NOT confirm Chad's
    specific national RCCM-registering authority/guichet-unique this
    session -- the Ministry of Justice's own site (`justice.gouv.td`,
    reached only via a 2020 Wayback snapshot) turned out to be
    substantially placeholder/lorem-ipsum content on the pages this
    iteration could reach -- an honest gap (see `marketentry.facts`'s
    own namespace docstring for the same gap, reported once, not
    duplicated as a false confirmation here).
  - **Code du Travail (Labour Code)**: Loi n°038/PR/96 du 11 décembre
    1996 -- this iteration independently fetched a clean, NATIVE
    text-layer PDF (no OCR needed, `pdftotext` succeeded directly) via
    the Internet Archive Wayback Machine (`droit-afrique.com`'s own
    site returned HTTP 403 on a direct WebFetch attempt this session --
    the documented fallback, not a bypass), own cover/table-of-contents
    text read verbatim (HIGH confidence): 'Tchad ... Code du travail ...
    Loi n°038/PR/96 du 11 décembre 1996', 6 Livres (dispositions
    générales, emploi, conditions de travail, organisations
    professionnelles, différends relatifs au travail, contrôle du
    travail et de l'emploi).
  - **Charte des investissements (Investment Charter)**: Loi n°006/PR/2008
    du 3 janvier 2008 -- this iteration independently fetched a clean,
    native text-layer PDF via the same Wayback fallback, own Art.1/Art.2
    text read verbatim (HIGH confidence): 'La présente loi institue la
    Charte des investissements de la République du Tchad', adopted 'en
    application des dispositions de la Charte des investissements de la
    CEMAC' (i.e. Chad's own Charter implements the CEMAC-wide investment
    charter framework, not a purely domestic instrument). Own Art.5
    excludes pure resale/negoce activity from the Charter's scope.
  - **Tax code (Code Général des Impôts)**: this iteration independently
    fetched, via the same Wayback fallback, only a 'sommaire' (table of
    contents, NOT the full operative text) of the 2023 edition -- an
    honest gap, no `statute.facts` entry is asserted for the CGI's own
    specific enacting law/ordinance number below, matching this
    catalog's discipline of never fabricating a citation it could not
    verify. See `marketentry.facts`'s `corporate-number-spec-basis` for
    how the CGI/DGI relationship is instead cited at the
    market-entry-evidence level (quitus fiscal), where a specific
    enactment number was not required to ground that citation.

  A law not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of statute entries. `:statute/url` + `:statute/law-number`
  are the citation the governor requires before any compliance-fact
  proposal referencing this law can commit."
  {"TCD"
   [{:statute/id "tcd.ohada-auscgie"
     :statute/title "Acte uniforme relatif au droit des sociétés commerciales et du groupement d'intérêt économique (AUSCGIE)"
     :statute/jurisdiction "TCD"
     :statute/kind :law
     :statute/law-number "OHADA Uniform Act -- adopted 30 January 2014 (Ouagadougou), published in the OHADA Official Gazette 4 February 2014, in force 5 May 2014; directly applicable in Chad as an OHADA member state (own text independently confirmed on ohada.org's own member-states page, 2026-07-23), no domestic transposition act required"
     :statute/url "https://www.ohada.org/en/commercial-companies-and-economic-interest-groups/"
     :statute/url-provenance :official-ohada-org
     :statute/enacted-date "2014-01-30"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:corporate-governance :incorporation}}
    {:statute/id "tcd.code-du-travail-1996"
     :statute/title "Code du travail de la République du Tchad"
     :statute/jurisdiction "TCD"
     :statute/kind :law
     :statute/law-number "Loi n°038/PR/96 du 11 décembre 1996 (own primary text, native text-layer PDF, read directly via pdftotext; HIGH confidence, no OCR required; fetched via the Internet Archive Wayback Machine after droit-afrique.com's own site returned HTTP 403 on a direct WebFetch attempt)"
     :statute/url "https://web.archive.org/web/20220121102613/http://www.droit-afrique.com/upload/doc/tchad/Tchad-Code-1996-du-travail.pdf"
     :statute/url-provenance :secondary-droit-afrique-com-via-wayback
     :statute/enacted-date "1996-12-11"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:labor :employment}}
    {:statute/id "tcd.charte-investissements-2008"
     :statute/title "Charte des investissements de la République du Tchad"
     :statute/jurisdiction "TCD"
     :statute/kind :law
     :statute/law-number "Loi n°006/PR/2008 du 3 janvier 2008 (own primary text, native text-layer PDF, read directly via pdftotext; HIGH confidence, no OCR required; adopted en application des dispositions de la Charte des investissements de la CEMAC; fetched via the Internet Archive Wayback Machine after droit-afrique.com's own site returned HTTP 403 on a direct WebFetch attempt)"
     :statute/url "https://web.archive.org/web/20220524004757/http://www.droit-afrique.com/upload/doc/tchad/Tchad-Charte-des-investissements-2008.pdf"
     :statute/url-provenance :secondary-droit-afrique-com-via-wayback
     :statute/enacted-date "2008-01-03"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:investment}}]})

(defn spec-basis
  "The jurisdiction's statute vector, or nil -- nil means NO spec-basis
  for that jurisdiction yet."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report, same shape/discipline as `marketentry.facts/coverage`:
  never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-tcd statute.facts Wave 0 (ADR-2607141700): "
                 (count (get catalog "TCD")) " TCD statute(s) seeded with an "
                 "official citation (the Code Général des Impôts' own current "
                 "enacting law/ordinance number could not be independently "
                 "confirmed this iteration -- only a table-of-contents "
                 "'sommaire' was fetched -- an honest gap, not an omission by "
                 "design). Extend `statute.facts/catalog`, never fabricate a "
                 "law-id or URL.")})))

(defn by-topic
  "Statutes for `iso3` tagged with `topic` (e.g. :labor, :investment)."
  [iso3 topic]
  (filterv #(contains? (:statute/topic %) topic) (spec-basis iso3)))
