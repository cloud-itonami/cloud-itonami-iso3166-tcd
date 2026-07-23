# cloud-itonami-iso3166-tcd

**TCD**: Chad.

- ARMP (Autorité de Régulation des Marchés Publics) / DGCMP (Direction
  Générale de Contrôle des Marchés Publics) public-procurement
  compliance (Décret N°2130/PR/2020 du 15 octobre 2020 portant Code
  des Marchés Publics)
- RCCM (OHADA AUDCG) business registration + DGI (Direction Générale
  des Impôts) tax registration; the Code's own Art.80 (préférence
  nationale) / Art.81 (préférence genre) bid-preference gates

AGPL-3.0-or-later.

## Market-entry / statute catalogs

Governed public-sector market-entry compliance actor, same architecture
as `cloud-itonami-iso3166-caf`/`-ner` (the closest architectural match:
both are also OHADA member states sharing the same supranational
company-law instrument, and both border Chad):

- `src/marketentry/{facts,governor,phase,sim,operation,registry,store,
  marketentryllm}.cljc` -- the actor. `facts.cljc` cites the current
  (15 October 2020) Code des Marchés Publics's own ARMP (Autorité de
  Régulation des Marchés Publics, an independent public institution
  under the Présidence de la République) / DGCMP (Direction Générale
  de Contrôle des Marchés Publics, a priori control) two-body split;
  RCCM (OHADA's Acte Uniforme relatif au Droit Commercial Général) and
  DGI (Direction Générale des Impôts) tax registration; Art.128(4)'s
  own real evidence-document list (non-faillite, domiciliation
  bancaire, caution de soumission, ARMP non-exclusion certificate,
  CNPS, patente, quitus fiscal); and Art.128(1)(e)'s conflict-of-
  interest exclusion ground. `governor.cljc`'s FLAGSHIP check
  independently recomputes eligibility for the Code's own **two
  textually distinct bid-preference regimes** -- Art.80 "préférence
  nationale" (15% price ceiling; Chadian-national artisans/individual
  entrepreneurs, or companies with a Chadian-capital majority held
  directly OR indirectly through a Chadian-law entity) and Art.81
  "préférence genre" (10% price ceiling; Chadian women-business-owner
  enterprises/groupings) -- a check shape genuinely different from
  every other iso3166 sibling's (see the namespace docstrings for the
  full research trail, the fleet-wide grep-verification of novelty,
  and honestly-flagged gaps this iteration could NOT verify, such as
  the specific Chad-national RCCM-registering agency).
- `src/statute/facts.cljc` -- general-law catalog: the OHADA Uniform
  Act on Commercial Companies (AUSCGIE, directly applicable, no
  domestic transposition act), Chad's own Code du Travail (Loi
  n°038/PR/96 du 11 décembre 1996) and Charte des investissements
  (Loi n°006/PR/2008 du 3 janvier 2008, implementing the CEMAC-wide
  investment charter). A Code Général des Impôts enacting-law-number
  citation could not be independently confirmed this iteration -- an
  honest gap; see the namespace docstring.

Every citation is curl/WebFetch/pdftotext-verified against an official
or official-adjacent source (armp-tchad.com's own site and its own
hosted decree PDFs, ohada.org, dgi.td). Where a primary Chadian
government site returned a bot-detection-adjacent block
(droit-afrique.com returned HTTP 403 on direct WebFetch), the Internet
Archive Wayback Machine was used as the documented fallback -- never a
browser-automation bypass. The current Code des Marchés Publics
(Décret N°2130/PR/2020) is a genuine scanned decree with an embedded,
systematically-noisy-but-legible OCR text layer this iteration
extracted directly via `pdftotext` -- see `marketentry.facts`'s
docstring for exactly which facts are HIGH-confidence (read directly,
native text-layer PDFs like the 2003 predecessor code, Code du
Travail, Charte des Investissements) vs. MODERATE-HIGH (the current
decree's OCR noise) vs. an honestly-flagged gap (the Ministry of
Justice's own site being substantially placeholder content; the exact
enacting number for the current Code Général des Impôts edition).

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for Chad:

- `src/culture/facts.cljc` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
