from pathlib import Path
import base64, io, tarfile, re

ROOT = Path(__file__).resolve().parents[1]
GEN = ROOT / 'app/src/main/java/com/example/data/GeneratedHarmonyContent.kt'
IMG = ROOT / 'app/src/main/java/com/example/ui/components/TotImageProvider.kt'
DRAW = ROOT / 'app/src/main/res/drawable'
PARTS = ROOT / 'tools/drive_tot_assets/parts'

chunks = sorted(PARTS.glob('part_*.txt'))
if chunks:
    payload = base64.b64decode(''.join(p.read_text().strip() for p in chunks))
    DRAW.mkdir(parents=True, exist_ok=True)
    with tarfile.open(fileobj=io.BytesIO(payload), mode='r:gz') as tf:
        for member in tf.getmembers():
            if member.isfile() and member.name.endswith('.webp') and '/' not in member.name:
                (DRAW / member.name).write_bytes(tf.extractfile(member).read())

g = GEN.read_text()
if '// DRIVE_TOT_CONTENT_BEGIN' not in g:
    anchor = '    val PACKS: List<GenPack> = listOf(\n'
    block = r'''        // DRIVE_TOT_CONTENT_BEGIN
        // Drive-backed visual "Das oder das?" packs. Existing Eis content is intentionally untouched.
        GenPack(id="reiseziele", title="Reiseziele", cat="tot", topic="reisen", type="tot", tags=listOf("dasoderdas","reisen"), pairs=listOf(
            "Paris, Frankreich" to "Rom, Italien", "Bali, Indonesien" to "Santorini, Griechenland", "London, England" to "New York, USA",
            "Malediven" to "Seychellen", "Tokyo, Japan" to "Dubai, VAE", "Venedig, Italien" to "Amsterdam, Niederlande", "Lappland, Finnland" to "Island",
            "Miami, USA" to "Bangkok, Thailand", "Chicago, USA" to "Barcelona, Spanien", "Lissabon, Portugal" to "Kopenhagen, Dänemark", "Prag, Tschechien" to "Budapest, Ungarn")),
        GenPack(id="traumhaus", title="Unser Traumhaus", cat="tot", topic="geld", type="tot", tags=listOf("dasoderdas"), pairs=listOf(
            "Altbau mit Charme" to "Neubau mit Smart Home", "Offene Wohnküche" to "Separate Küche", "Prasselnder Kamin" to "Fußbodenheizung", "Großer Garten" to "Sonnige Dachterrasse",
            "Stadtvilla" to "Landhaus", "Glasfassade" to "Natursteinfassade", "Penthouse mit Ausblick" to "Haus am See", "Minimalistisches Interieur" to "Landhausstil",
            "Bibliothek" to "Heimkino", "Innenpool" to "Wellnessbad", "Große Fensterfront" to "Privater Innenhof", "Tiny House" to "Mehrgenerationenhaus")),
        GenPack(id="aussen", title="Traumhaus Außenbereich", cat="tot", topic="geld", type="tot", tags=listOf("dasoderdas"), pairs=listOf(
            "Großer Außenpool" to "Outdoor-Whirlpool", "Moderne Grillstation" to "Gemütliche Feuerstelle", "Eigenes Gemüsebeet" to "Bunte Blumenwiese", "Entspannte Hängematte" to "Stilvolles Outdoor-Sofa",
            "Infinity-Pool" to "Naturteich", "Outdoor-Küche" to "Pizzaofen", "Pergola mit Lounge" to "Wintergarten", "Kräuterbeet" to "Obstgarten", "Dachgarten mit Lounge" to "Mediterraner Innenhof",
            "Feuerstelle" to "Außenkamin", "Spielbereich für Kinder" to "Sportplatz", "Gewächshaus" to "Saunahaus")),
        GenPack(id="aktivitaeten", title="Aktivitäten & Hobbys", cat="tot", topic="hobbys", type="tot", tags=listOf("dasoderdas","hobbys"), pairs=listOf(
            "Wandern" to "Strandtag", "Konzert" to "Kino", "Kochkurs" to "Restaurant", "Museum" to "Freizeitpark", "Töpfern" to "Klavier spielen", "Malen" to "Zeichnen",
            "Badminton" to "Mountainbike", "Bowling" to "Holzwerken", "Gitarre spielen" to "Tennis", "Brettspiele" to "Darts")),
        GenPack(id="ringe", title="Verlobungsringe", cat="tot", topic="beziehung", type="tot", tags=listOf("hochzeit","dasoderdas"), pairs=listOf(
            "Klassisch Solitär" to "Vintage verspielt", "Gelbgold" to "Weißgold", "Großer Stein" to "Filigran & schlicht", "Diamant" to "Farbedelstein", "Platin" to "Roségold",
            "Drei-Stein-Ring" to "Moderner Solitär", "Ovaler Diamant" to "Runder Diamant", "Schmal & zart" to "Markant & breit", "Moissanit" to "Saphir",
            "Vintage Art déco" to "Modern geometrisch", "Gravur innen" to "Diamanten im Band", "Ohne Stein" to "Statement-Ring")),
        GenPack(id="getraenke", title="Getränke", cat="tot", topic="essen", type="tot", tags=listOf("dasoderdas","getraenke"), pairs=listOf(
            "Cappuccino" to "Matcha-Latte", "Heiße Schokolade" to "Eistee", "Minzlimonade" to "Fruchtpunsch", "Bier" to "Rote-Bete-Saft", "Coca-Cola" to "Fanta", "Orangensaft" to "Apfelsaft", "Kaffee" to "Tee")),
        GenPack(id="tiere", title="Tiere", cat="tot", topic="kennen", type="tot", tags=listOf("dasoderdas","tiere"), pairs=listOf(
            "Hund" to "Katze", "Singvogel" to "Pinguin", "Kaninchen" to "Otter", "Roter Panda" to "Fuchs", "Meerschweinchen" to "Giraffe", "Löwe" to "Gorilla",
            "Meeresschildkröte" to "Igel", "Tiger" to "Wolf", "Adler" to "Delfin")),
        // DRIVE_TOT_CONTENT_END
'''
    if anchor not in g: raise SystemExit('PACKS anchor not found')
    GEN.write_text(g.replace(anchor, anchor + block, 1))

t = IMG.read_text()
if '// DRIVE_TOT_IMAGE_MAP_BEGIN' not in t:
    anchor = '    private val directMap: Map<String, Any> = mapOf(\n'
    entries = r'''        // DRIVE_TOT_IMAGE_MAP_BEGIN
        "Cappuccino" to R.drawable.getraenk_cappuccino, "Matcha-Latte" to R.drawable.getraenk_matcha,
        "Heiße Schokolade" to R.drawable.getraenk_heisse_schokolade, "Eistee" to R.drawable.getraenk_eistee,
        "Minzlimonade" to R.drawable.getraenk_minzlimonade, "Fruchtpunsch" to R.drawable.getraenk_fruchtpunsch,
        "Bier" to R.drawable.getraenk_bier, "Rote-Bete-Saft" to R.drawable.getraenk_rote_bete,
        "Coca-Cola" to R.drawable.getraenk_cola, "Fanta" to R.drawable.getraenk_fanta,
        "Orangensaft" to R.drawable.getraenk_orangensaft, "Apfelsaft" to R.drawable.getraenk_apfelsaft,
        "Kaffee" to R.drawable.getraenk_kaffee, "Tee" to R.drawable.getraenk_tee,
        "Hund" to R.drawable.tier_hund, "Katze" to R.drawable.tier_katze, "Singvogel" to R.drawable.tier_singvogel, "Pinguin" to R.drawable.tier_pinguin,
        "Kaninchen" to R.drawable.tier_kaninchen, "Otter" to R.drawable.tier_otter, "Roter Panda" to R.drawable.tier_roter_panda, "Fuchs" to R.drawable.tier_fuchs,
        "Meerschweinchen" to R.drawable.tier_meerschweinchen, "Giraffe" to R.drawable.tier_giraffe, "Löwe" to R.drawable.tier_loewe, "Gorilla" to R.drawable.tier_gorilla,
        "Meeresschildkröte" to R.drawable.tier_meeresschildkroete, "Igel" to R.drawable.tier_igel, "Tiger" to R.drawable.tier_tiger, "Wolf" to R.drawable.tier_wolf,
        "Adler" to R.drawable.tier_adler, "Delfin" to R.drawable.tier_delfin,
        "Töpfern" to R.drawable.hobby_toepfern, "Klavier spielen" to R.drawable.hobby_klavier, "Malen" to R.drawable.hobby_malen, "Zeichnen" to R.drawable.hobby_zeichnen,
        "Badminton" to R.drawable.hobby_badminton, "Mountainbike" to R.drawable.hobby_mountainbike, "Bowling" to R.drawable.hobby_bowling, "Holzwerken" to R.drawable.hobby_holzwerken,
        "Gitarre spielen" to R.drawable.hobby_gitarre, "Tennis" to R.drawable.hobby_tennis, "Brettspiele" to R.drawable.hobby_brettspiel, "Darts" to R.drawable.hobby_darts,
        "Miami, USA" to "https://images.unsplash.com/photo-1535498730771-e735b998cd64?w=800&auto=format&fit=crop&q=80",
        "Bangkok, Thailand" to "https://images.unsplash.com/photo-1508009603885-50cf7c579365?w=800&auto=format&fit=crop&q=80",
        "Chicago, USA" to "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=800&auto=format&fit=crop&q=80",
        "Barcelona, Spanien" to "https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800&auto=format&fit=crop&q=80",
        "Lissabon, Portugal" to "https://images.unsplash.com/photo-1555881400-74d7acaacd8b?w=800&auto=format&fit=crop&q=80",
        "Kopenhagen, Dänemark" to "https://images.unsplash.com/photo-1513622470522-26c3c8a854bc?w=800&auto=format&fit=crop&q=80",
        "Prag, Tschechien" to "https://images.unsplash.com/photo-1541849546-216549ae216d?w=800&auto=format&fit=crop&q=80",
        "Budapest, Ungarn" to "https://images.unsplash.com/photo-1549877452-9c387954fbc2?w=800&auto=format&fit=crop&q=80",
        // DRIVE_TOT_IMAGE_MAP_END
'''
    if anchor not in t: raise SystemExit('directMap anchor not found')
    t = t.replace(anchor, anchor + entries, 1)

local_map = {
'Tokyo, Japan':'R.drawable.tokyo_tower_zojoji','Tokyo':'R.drawable.tokyo_tower_zojoji','Wandern':'R.drawable.hobby_wandern',
'Altbau mit Charme':'R.drawable.traumhaus_altbau','Neubau mit Smart Home':'R.drawable.traumhaus_smart_home','Offene Wohnküche':'R.drawable.traumhaus_wohnkueche','Separate Küche':'R.drawable.traumhaus_separate_kueche','Prasselnder Kamin':'R.drawable.traumhaus_kamin','Fußbodenheizung':'R.drawable.traumhaus_fussbodenheizung','Großer Garten':'R.drawable.traumhaus_garten','Sonnige Dachterrasse':'R.drawable.traumhaus_dachterrasse','Stadtvilla':'R.drawable.traumhaus_stadtvilla','Landhaus':'R.drawable.traumhaus_landhaus','Glasfassade':'R.drawable.traumhaus_glasfassade','Natursteinfassade':'R.drawable.traumhaus_naturstein','Penthouse mit Ausblick':'R.drawable.traumhaus_penthouse','Haus am See':'R.drawable.traumhaus_see','Minimalistisches Interieur':'R.drawable.traumhaus_minimal','Landhausstil':'R.drawable.traumhaus_landhausstil','Bibliothek':'R.drawable.traumhaus_bibliothek','Heimkino':'R.drawable.traumhaus_heimkino','Innenpool':'R.drawable.traumhaus_innenpool','Wellnessbad':'R.drawable.traumhaus_wellnessbad','Große Fensterfront':'R.drawable.traumhaus_fensterfront','Privater Innenhof':'R.drawable.traumhaus_innenhof','Tiny House':'R.drawable.traumhaus_tiny','Mehrgenerationenhaus':'R.drawable.traumhaus_mehrgenerationenhaus',
'Großer Außenpool':'R.drawable.aussen_pool','Outdoor-Whirlpool':'R.drawable.aussen_whirlpool','Moderne Grillstation':'R.drawable.aussen_grill','Gemütliche Feuerstelle':'R.drawable.aussen_feuerstelle','Eigenes Gemüsebeet':'R.drawable.aussen_gemuesebeet','Bunte Blumenwiese':'R.drawable.aussen_blumenwiese','Entspannte Hängematte':'R.drawable.aussen_haengematte','Stilvolles Outdoor-Sofa':'R.drawable.aussen_sofa','Infinity-Pool':'R.drawable.aussen_infinity','Naturteich':'R.drawable.aussen_naturteich','Outdoor-Küche':'R.drawable.aussen_outdoor_kueche','Pizzaofen':'R.drawable.aussen_pizzaofen','Pergola mit Lounge':'R.drawable.aussen_pergola','Wintergarten':'R.drawable.aussen_wintergarten','Kräuterbeet':'R.drawable.aussen_kraeuter','Obstgarten':'R.drawable.aussen_obstgarten','Dachgarten mit Lounge':'R.drawable.aussen_dachgarten','Mediterraner Innenhof':'R.drawable.aussen_mediterraner_innenhof','Feuerstelle':'R.drawable.aussen_feuerstelle_neu','Außenkamin':'R.drawable.aussen_aussenkamin','Spielbereich für Kinder':'R.drawable.aussen_spielbereich','Sportplatz':'R.drawable.aussen_sportplatz','Gewächshaus':'R.drawable.aussen_gewaechshaus','Saunahaus':'R.drawable.aussen_saunahaus',
'Klassisch Solitär':'R.drawable.ring_klassisch_solitaer','Vintage verspielt':'R.drawable.ring_vintage_verspielt','Gelbgold':'R.drawable.ring_gelbgold','Weißgold':'R.drawable.ring_weissgold','Großer Stein':'R.drawable.ring_grosser_stein','Filigran & schlicht':'R.drawable.ring_filigran_schlicht','Diamant':'R.drawable.ring_diamant','Farbedelstein':'R.drawable.ring_farbedelstein','Platin':'R.drawable.ring_platin','Roségold':'R.drawable.ring_rosegold','Drei-Stein-Ring':'R.drawable.ring_drei_stein','Moderner Solitär':'R.drawable.ring_moderner_solitaer','Ovaler Diamant':'R.drawable.ring_ovaler_diamant','Runder Diamant':'R.drawable.ring_runder_diamant','Schmal & zart':'R.drawable.ring_schmal_zart','Markant & breit':'R.drawable.ring_markant_breit','Moissanit':'R.drawable.ring_moissanit','Saphir':'R.drawable.ring_saphir','Vintage Art déco':'R.drawable.ring_art_deco','Modern geometrisch':'R.drawable.ring_modern_geometrisch','Gravur innen':'R.drawable.ring_gravur_innen','Diamanten im Band':'R.drawable.ring_diamanten_band','Ohne Stein':'R.drawable.ring_ohne_stein','Statement-Ring':'R.drawable.ring_statement'}
for key,value in local_map.items():
    t = re.sub(r'("'+re.escape(key)+r'"\s+to\s+)([^,\n]+)', lambda m:m.group(1)+value, t, count=1)
IMG.write_text(t)
print('Drive TOT content and images applied.')
