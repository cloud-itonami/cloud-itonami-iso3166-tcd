(ns culture.facts
  "Country-level regional-culture catalog for Chad (TCD) -- national
  dishes, protected products, beverages, crafts, festivals and heritage
  sites, per ADR-2607171400 addendum 2 (cloud-itonami-municipality-
  culture-catalog Wave 1, in com-junkawasaki/root). Sibling namespace to
  `marketentry.facts` / `statute.facts` (ADR-2607141700); city-level
  counterparts live in the cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"TCD"
   [{:culture/id "tcd.dish.boule"
     :culture/name "Boule"
     :culture/country "TCD"
     :culture/kind :dish
     :culture/summary "Thick Chadian porridge made from grains such as millet, sorghum or maize."
     :culture/url "https://en.wikipedia.org/wiki/Chadian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "tcd.dish.daraba"
     :culture/name "Daraba"
     :culture/country "TCD"
     :culture/kind :dish
     :culture/summary "Traditional Chadian dish prepared with okra, tomatoes, sweet potatoes, greens and peanut butter or paste."
     :culture/url "https://en.wikipedia.org/wiki/Chadian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "tcd.dish.jarret-de-boeuf"
     :culture/name "Jarret de boeuf"
     :culture/country "TCD"
     :culture/kind :dish
     :culture/summary "Traditional Chadian beef and vegetable stew requiring at least two hours of stewing."
     :culture/url "https://en.wikipedia.org/wiki/Chadian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "tcd.beverage.karkanji"
     :culture/name "Karkanji"
     :culture/name-local "Carcaje"
     :culture/country "TCD"
     :culture/kind :beverage
     :culture/summary "Red tea made from dried hibiscus flowers, with ginger, clove, cinnamon and sugar added to taste, drunk in Chad."
     :culture/url "https://en.wikipedia.org/wiki/Chadian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "tcd.beverage.bili-bili"
     :culture/name "Bili-bili"
     :culture/country "TCD"
     :culture/kind :beverage
     :culture/summary "Millet beer consumed in the southern regions of Chad."
     :culture/url "https://en.wikipedia.org/wiki/Chadian_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "tcd.heritage.ennedi-massif"
     :culture/name "Ennedi Massif"
     :culture/country "TCD"
     :culture/kind :heritage
     :culture/summary "Natural and cultural landscape in Chad, inscribed as a UNESCO World Heritage Site in February 2016 at the 40th Session of the World Heritage Committee."
     :culture/url "https://en.wikipedia.org/wiki/Ennedi_Plateau"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "tcd.heritage.lakes-of-ounianga"
     :culture/name "Lakes of Ounianga"
     :culture/country "TCD"
     :culture/kind :heritage
     :culture/summary "Group of lakes in Chad's Ennedi Ouest region, inscribed as a UNESCO World Heritage Site in 2012 for their exceptional natural characteristics within the Sahara Desert."
     :culture/url "https://en.wikipedia.org/wiki/Lakes_of_Ounianga"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-tcd culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "TCD"))
                 " TCD entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
