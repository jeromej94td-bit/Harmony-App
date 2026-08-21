package com.example.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.R
import com.example.ui.EXACT_DANISH_CONTENT
import com.example.ui.EXACT_ENGLISH_CONTENT
import com.example.ui.EXACT_FRENCH_CONTENT
import com.example.ui.EXACT_ITALIAN_CONTENT
import com.example.ui.EXACT_JAPANESE_CONTENT
import com.example.ui.EXACT_NORWEGIAN_CONTENT
import com.example.ui.EXACT_PORTUGUESE_BRAZIL_CONTENT
import com.example.ui.EXACT_PORTUGUESE_CONTENT
import com.example.ui.EXACT_PORTUGUESE_PORTUGAL_CONTENT
import com.example.ui.EXACT_SPANISH_LATIN_AMERICA_CONTENT
import com.example.ui.EXACT_SPANISH_SPAIN_CONTENT
import java.io.File

/**
 * Liefert zu jedem Options-Text ein Bild.
 *
 * Reihenfolge:
 *   1. userOverrides      -> im Dev Studio von Hand gesetzt
 *   2. generatedOverrides -> aus GeneratedHarmonyContent.kt (Export)
 *   3. directMap          -> fest eingebaute Standardbilder
 *   4. Stichwort-Heuristik
 *
 * Werte dürfen sein: http(s)-URL, absoluter Dateipfad (/data/...), oder eine Res-ID.
 */
object TotImageProvider {

    /**
     * Zähler, der bei jeder Bildänderung hochgeht.
     * Compose-Aufrufer nutzen ihn als remember-Key, damit ein neu gesetztes Bild
     * sofort sichtbar wird — ohne die App neu zu starten.
     */
    var version by mutableStateOf(0)
        private set


    private val directMap: Map<String, Any> = mapOf(
        // DRIVE_TOT_RELEASE_IMAGES_BEGIN
        "Cappuccino" to "https://loremflickr.com/800/600/cappuccino?lock=1",
        "Matcha-Latte" to "https://loremflickr.com/800/600/matcha-latte?lock=2",
        "Heiße Schokolade" to "https://loremflickr.com/800/600/hot-chocolate?lock=3",
        "Eistee" to "https://loremflickr.com/800/600/iced-tea?lock=4",
        "Minzlimonade" to "https://loremflickr.com/800/600/mint-lemonade?lock=5",
        "Fruchtpunsch" to "https://loremflickr.com/800/600/fruit-punch?lock=6",
        "Bier" to "https://loremflickr.com/800/600/beer?lock=7",
        "Rote-Bete-Saft" to "https://loremflickr.com/800/600/beetroot-juice?lock=8",
        "Coca-Cola" to "https://loremflickr.com/800/600/cola?lock=9",
        "Fanta" to "https://loremflickr.com/800/600/orange-soda?lock=10",
        "Orangensaft" to "https://loremflickr.com/800/600/orange-juice?lock=11",
        "Apfelsaft" to "https://loremflickr.com/800/600/apple-juice?lock=12",
        "Kaffee" to "https://loremflickr.com/800/600/coffee?lock=13",
        "Tee" to "https://loremflickr.com/800/600/tea?lock=14",
        "Hund" to "https://loremflickr.com/800/600/puppy?lock=15",
        "Katze" to "https://loremflickr.com/800/600/kitten?lock=16",
        "Singvogel" to "https://loremflickr.com/800/600/songbird?lock=17",
        "Pinguin" to "https://loremflickr.com/800/600/penguin?lock=18",
        "Kaninchen" to "https://loremflickr.com/800/600/rabbit?lock=19",
        "Otter" to "https://loremflickr.com/800/600/otter?lock=20",
        "Roter Panda" to "https://loremflickr.com/800/600/red-panda?lock=21",
        "Fuchs" to "https://loremflickr.com/800/600/fox?lock=22",
        "Meerschweinchen" to "https://loremflickr.com/800/600/guinea-pig?lock=23",
        "Giraffe" to "https://loremflickr.com/800/600/giraffe?lock=24",
        "Löwe" to "https://loremflickr.com/800/600/lion?lock=25",
        "Gorilla" to "https://loremflickr.com/800/600/gorilla?lock=26",
        "Meeresschildkröte" to "https://loremflickr.com/800/600/sea-turtle?lock=27",
        "Igel" to "https://loremflickr.com/800/600/hedgehog?lock=28",
        "Tiger" to "https://loremflickr.com/800/600/tiger?lock=29",
        "Wolf" to "https://loremflickr.com/800/600/wolf?lock=30",
        "Adler" to "https://loremflickr.com/800/600/eagle?lock=31",
        "Delfin" to "https://loremflickr.com/800/600/dolphin?lock=32",
        "Töpfern" to "https://loremflickr.com/800/600/pottery?lock=33",
        "Klavier spielen" to "https://loremflickr.com/800/600/piano?lock=34",
        "Malen" to "https://loremflickr.com/800/600/painting?lock=35",
        "Zeichnen" to "https://loremflickr.com/800/600/drawing?lock=36",
        "Badminton" to "https://loremflickr.com/800/600/badminton?lock=37",
        "Mountainbike" to "https://loremflickr.com/800/600/mountain-bike?lock=38",
        "Bowling" to "https://loremflickr.com/800/600/bowling?lock=39",
        "Holzwerken" to "https://loremflickr.com/800/600/woodworking?lock=40",
        "Gitarre spielen" to "https://loremflickr.com/800/600/acoustic-guitar?lock=41",
        "Tennis" to "https://loremflickr.com/800/600/tennis?lock=42",
        "Brettspiele" to "https://loremflickr.com/800/600/board-game?lock=43",
        "Darts" to "https://loremflickr.com/800/600/darts?lock=44",
        "Miami, USA" to "https://images.unsplash.com/photo-1535498730771-e735b998cd64?w=800&auto=format&fit=crop&q=80",
        "Bangkok, Thailand" to "https://images.unsplash.com/photo-1508009603885-50cf7c579365?w=800&auto=format&fit=crop&q=80",
        "Chicago, USA" to "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=800&auto=format&fit=crop&q=80",
        "Barcelona, Spanien" to "https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800&auto=format&fit=crop&q=80",
        "Lissabon, Portugal" to "https://images.unsplash.com/photo-1555881400-74d7acaacd8b?w=800&auto=format&fit=crop&q=80",
        "Kopenhagen, Dänemark" to "https://images.unsplash.com/photo-1513622470522-26c3c8a854bc?w=800&auto=format&fit=crop&q=80",
        "Prag, Tschechien" to "https://images.unsplash.com/photo-1541849546-216549ae216d?w=800&auto=format&fit=crop&q=80",
        "Budapest, Ungarn" to "https://images.unsplash.com/photo-1549877452-9c387954fbc2?w=800&auto=format&fit=crop&q=80",
        // DRIVE_TOT_RELEASE_IMAGES_END
        // ★ Reiseziele
        "Paris, Frankreich" to "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&auto=format&fit=crop&q=80",
        "Rom, Italien" to "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800&auto=format&fit=crop&q=80",
        "Bali, Indonesien" to "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800&auto=format&fit=crop&q=80",
        "Santorini, Griechenland" to "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?w=800&auto=format&fit=crop&q=80",
        "London, England" to "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800&auto=format&fit=crop&q=80",
        "New York, USA" to "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=800&auto=format&fit=crop&q=80",
        "Tokyo, Japan" to R.drawable.tokyo_tower_zojoji,
        "Dubai, VAE" to "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80",
        "Venedig, Italien" to "https://images.unsplash.com/photo-1514890547357-a9ee288728e0?w=800&auto=format&fit=crop&q=80",
        "Amsterdam, Niederlande" to "https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?w=800&auto=format&fit=crop&q=80",
        "Lappland, Finnland" to "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?w=800&auto=format&fit=crop&q=80",
        "Bali" to "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800&auto=format&fit=crop&q=80",
        "Santorini" to "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?w=800&auto=format&fit=crop&q=80",
        "London" to "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800&auto=format&fit=crop&q=80",
        "Paris" to "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&auto=format&fit=crop&q=80",
        "Rom" to "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800&auto=format&fit=crop&q=80",
        "Malediven" to "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=800&auto=format&fit=crop&q=80",
        "Seychellen" to "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&auto=format&fit=crop&q=80",
        "New York" to "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=800&auto=format&fit=crop&q=80",
        "Dubai" to "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80",
        "Tokyo" to R.drawable.tokyo_tower_zojoji,
        "Las Vegas" to "https://images.unsplash.com/photo-1506146332389-18140dc7b2fb?w=800&auto=format&fit=crop&q=80",
        "Venedig" to "https://images.unsplash.com/photo-1514890547357-a9ee288728e0?w=800&auto=format&fit=crop&q=80",
        "Amsterdam" to "https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?w=800&auto=format&fit=crop&q=80",
        "Lappland" to "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?w=800&auto=format&fit=crop&q=80",
        "Island" to "https://images.unsplash.com/photo-1504893524553-b855bce32c67?w=800&auto=format&fit=crop&q=80",
        "Monaco" to "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800&auto=format&fit=crop&q=80",
        "Nizza" to "https://images.unsplash.com/photo-1533929736458-ca588d08c8be?w=800&auto=format&fit=crop&q=80",

        // ★ Traumhaus & Außenbereich
        "Altbau mit Charme" to "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&auto=format&fit=crop&q=80",
        "Neubau mit Smart Home" to "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&auto=format&fit=crop&q=80",
        "Neubau mit Technik" to "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&auto=format&fit=crop&q=80",
        "Offene Wohnküche" to "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&auto=format&fit=crop&q=80",
        "Offene Küche" to "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&auto=format&fit=crop&q=80",
        "Separate Küche" to "https://images.unsplash.com/photo-1507089947368-19c1da9775ae?w=800&auto=format&fit=crop&q=80",
        "Prasselnder Kamin" to "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80",
        "Kamin" to "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80",
        "Fußbodenheizung" to "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800&auto=format&fit=crop&q=80",
        "Großer Garten" to "https://images.unsplash.com/photo-1558904541-efa843a96f01?w=800&auto=format&fit=crop&q=80",
        "Dachterrasse" to "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800&auto=format&fit=crop&q=80",
        "Sonnige Dachterrasse" to "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800&auto=format&fit=crop&q=80",
        "Großer Außenpool" to "https://images.unsplash.com/photo-1576013551627-0cc20b96c2a7?w=800&auto=format&fit=crop&q=80",
        "Pool" to "https://images.unsplash.com/photo-1576013551627-0cc20b96c2a7?w=800&auto=format&fit=crop&q=80",
        "Outdoor-Whirlpool" to "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80",
        "Whirlpool" to "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80",
        "Jacuzzi" to "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80",
        "Hot Tub" to "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80",
        "Moderne Grillstation" to "https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?w=800&auto=format&fit=crop&q=80",
        "Grillplatz" to "https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?w=800&auto=format&fit=crop&q=80",
        "Gemütliche Feuerstelle" to "https://images.unsplash.com/photo-1517824806704-9040b037703b?w=800&auto=format&fit=crop&q=80",
        "Feuerstelle" to "https://images.unsplash.com/photo-1517824806704-9040b037703b?w=800&auto=format&fit=crop&q=80",
        "Eigenes Gemüsebeet" to "https://images.unsplash.com/photo-1592417817098-8f3d6eb12765?w=800&auto=format&fit=crop&q=80",
        "Gemüsebeet" to "https://images.unsplash.com/photo-1592417817098-8f3d6eb12765?w=800&auto=format&fit=crop&q=80",
        "Bunte Blumenwiese" to "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=800&auto=format&fit=crop&q=80",
        "Blumenwiese" to "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=800&auto=format&fit=crop&q=80",
        "Entspannte Hängematte" to "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80",
        "Hängematte" to "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80",
        "Stilvolles Outdoor-Sofa" to "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=800&auto=format&fit=crop&q=80",
        "Outdoor-Sofa" to "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=800&auto=format&fit=crop&q=80",

        // ★ Aktivitäten & Essen
        "Wandern" to "https://images.unsplash.com/photo-1551632811-561732d1e306?w=800&auto=format&fit=crop&q=80",
        "Strandtag" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
        "Konzert" to "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80",
        "Kino" to "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
        "Kochkurs" to "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=800&auto=format&fit=crop&q=80",
        "Restaurant" to "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80",
        "Museum" to "https://images.unsplash.com/photo-1565008447742-97f6f38c985c?w=800&auto=format&fit=crop&q=80",
        "Freizeitpark" to "https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?w=800&auto=format&fit=crop&q=80",
        "Pizza" to "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80",
        "Pasta" to "https://images.unsplash.com/photo-1621996346565-e3d5d6281216?w=800&auto=format&fit=crop&q=80",
        "Sushi" to "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800&auto=format&fit=crop&q=80",
        "Burger" to "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
        "Süß" to "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800&auto=format&fit=crop&q=80",
        "Herzhaft" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=800&auto=format&fit=crop&q=80",
        "Selbst kochen" to "https://images.unsplash.com/photo-1507048331197-7d4ac70811cf?w=800&auto=format&fit=crop&q=80",
        "Bestellen" to "https://images.unsplash.com/photo-1526367790999-0150786686a2?w=800&auto=format&fit=crop&q=80",

        // ★ Ringe & Hochzeit & Sträuße
        "Klassisch Solitär" to "https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=800&auto=format&fit=crop&q=80",
        "Vintage verspielt" to "https://images.unsplash.com/photo-1603561591411-07134e71a2a9?w=800&auto=format&fit=crop&q=80",
        "Gelbgold" to "https://images.unsplash.com/photo-1602751584552-8ba73aad10e1?w=800&auto=format&fit=crop&q=80",
        "Weißgold" to "https://images.unsplash.com/photo-1598560917505-59a3ad559071?w=800&auto=format&fit=crop&q=80",
        "Großer Stein" to "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=800&auto=format&fit=crop&q=80",
        "Filigran & schlicht" to "https://images.unsplash.com/photo-1515562141207-6811bcb33efb?w=800&auto=format&fit=crop&q=80",
        "Diamant" to "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=800&auto=format&fit=crop&q=80",
        "Farbedelstein" to "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=800&auto=format&fit=crop&q=80",
        "Weiße Rosen" to "https://images.unsplash.com/photo-1561181286-d3fee7d55364?w=800&auto=format&fit=crop&q=80",
        "Pfingstrosen" to "https://images.unsplash.com/photo-1526047932273-341f2a7631f9?w=800&auto=format&fit=crop&q=80",
        "Wildblumen" to "https://images.unsplash.com/photo-1508615039623-a25605d2b022?w=800&auto=format&fit=crop&q=80",
        "Klassisch gebunden" to "https://images.unsplash.com/photo-1519225421980-715cb0215aed?w=800&auto=format&fit=crop&q=80",
        "Groß & üppig" to "https://images.unsplash.com/photo-1523438885200-e635ba2c371e?w=800&auto=format&fit=crop&q=80",
        "Klein & zart" to "https://images.unsplash.com/photo-1527061011665-3652c757a4d4?w=800&auto=format&fit=crop&q=80",
        "Pastell" to "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
        "Kräftige Farben" to "https://images.unsplash.com/photo-1508615039623-a25605d2b022?w=800&auto=format&fit=crop&q=80",
        "Große Feier" to "https://images.unsplash.com/photo-1519741497674-611481863552?w=800&auto=format&fit=crop&q=80",
        "Kleine Runde" to "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?w=800&auto=format&fit=crop&q=80",
        "Am Strand" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
        "In den Bergen" to "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80",
        "Kirchlich" to "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=800&auto=format&fit=crop&q=80",
        "Standesamt & Party" to "https://images.unsplash.com/photo-1519167758481-dc8997617474?w=800&auto=format&fit=crop&q=80",
        "Sommerhochzeit" to "https://images.unsplash.com/photo-1519225421980-715cb0215aed?w=800&auto=format&fit=crop&q=80",
        "Winterhochzeit" to "https://images.unsplash.com/photo-1482517967863-00e15c9b44be?w=800&auto=format&fit=crop&q=80",

        // ★ Was magst du lieber?
        "Ein Filmabend" to "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800&auto=format&fit=crop&q=80",
        "Ein Spieleabend" to "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800&auto=format&fit=crop&q=80",
        "Ein aktives Abenteuer" to "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=800&auto=format&fit=crop&q=80",
        "Ein entspannender Spa-Tag" to "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800&auto=format&fit=crop&q=80",
        "Ein gemütliches Date drinnen" to "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800&auto=format&fit=crop&q=80",
        "Eine Autoreise" to "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&auto=format&fit=crop&q=80",
        "Einen Film anschauen" to "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
        "Gemeinsam ein Lego bauen" to "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=800&auto=format&fit=crop&q=80",
        "Eine Weinverkostung" to "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=800&auto=format&fit=crop&q=80",
        "Eine Schokoladenverkostung" to "https://images.unsplash.com/photo-1511381939415-e44015466834?w=800&auto=format&fit=crop&q=80",
        "Eine gemütliche Nacht zu Hause" to "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800&auto=format&fit=crop&q=80",
        "Ein Abenteuer in einer neuen Stadt" to "https://images.unsplash.com/photo-1477959858617-67f30ac4ce78?w=800&auto=format&fit=crop&q=80",
        "Zu einem Picknick gehen" to "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=800&auto=format&fit=crop&q=80",
        "Zu einem ausgefallenen Abendessen gehen" to "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80",
        "Ein romantischer Abend" to "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop&q=80",
        "Eine Nacht in einem Club" to "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80",
        "Eine Date-Nacht unter den Sternen" to "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&auto=format&fit=crop&q=80",
        "Ein romantisches Abendessen bei Kerzenlicht" to "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop&q=80",
        "In einen Coffeeshop gehen" to "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&auto=format&fit=crop&q=80",
        "In eine Bar gehen" to "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800&auto=format&fit=crop&q=80",
        "Ein gemütlicher Abend im Haus während eines Gewitters" to "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=800&auto=format&fit=crop&q=80",
        "Ein tolles Date im Freien unter dem Mondlicht" to "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&auto=format&fit=crop&q=80",
        "Einen Vergnügungspark erkunden" to "https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?w=800&auto=format&fit=crop&q=80",
        "Ein Museum besuchen" to "https://images.unsplash.com/photo-1565008447742-97f6f38c985c?w=800&auto=format&fit=crop&q=80",
        "Ein Spieleabend mit Freunden" to "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800&auto=format&fit=crop&q=80",
        "Ein romantisches Picknick an einem schönen Ort" to "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=800&auto=format&fit=crop&q=80",
        "Gemeinsam in der Küche ein neues Rezept ausprobieren" to "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&auto=format&fit=crop&q=80",
        "In einem feinen Restaurant essen gehen" to "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80",
        "Zelten gehen" to "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80",
        "Einen Wellness-Tag" to "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800&auto=format&fit=crop&q=80",
        "Einen Tanzkurs besuchen" to "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=800&auto=format&fit=crop&q=80",
        "Eine Wanderung mit Panoramablick machen" to "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80",
        "Spiele spielen und Spaß haben" to "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800&auto=format&fit=crop&q=80",
        "Tiefgründige Gespräche führen" to "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&auto=format&fit=crop&q=80",
        "Ein Live-Musik-Konzert besuchen" to "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80",
        "Auf eine Bootsparty gehen" to "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&auto=format&fit=crop&q=80",
        "Geh früh am Morgen, um den Sonnenaufgang zu sehen" to "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=800&auto=format&fit=crop&q=80",
        "In eine Strandbar gehen, um den Sonnenuntergang zu sehen" to "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?w=800&auto=format&fit=crop&q=80",

        // ★ Liebe im Gleichgewicht
        "Lass dich von deinem Partner inspirieren, dein bestes Selbst zu sein" to "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=800&auto=format&fit=crop&q=80",
        "Werde so akzeptiert, wie du bist" to "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80",
        "Ein Jahr lang eine Fernbeziehung führen" to "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=80",
        "Einen Monat lang überhaupt nicht miteinander reden" to "https://images.unsplash.com/photo-1509114397022-ed747cca3f65?w=800&auto=format&fit=crop&q=80",
        "Intime Momente nur dann zu haben, wenn dein Partner sie initiiert" to "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80",
        "Alle intimen Momente selbst initiieren" to "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop&q=80",
        "Deine tiefsten Geheimnisse lieber mit deinem Partner teilen" to "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&auto=format&fit=crop&q=80",
        "Einige Dinge für dich behalten" to "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800&auto=format&fit=crop&q=80",
        "Eine Million Dollar gewinnen" to "https://images.unsplash.com/photo-1518458028785-8fbcd101ebb9?w=800&auto=format&fit=crop&q=80",
        "Eine Million Dollar verdienen" to "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=80",
        "Einen sehr emotionalen Partner haben" to "https://images.unsplash.com/photo-1494774157365-9e04c6720e47?w=800&auto=format&fit=crop&q=80",
        "Einen sehr logischen Partner haben" to "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
        "Deine Beziehung stabil und sicher machen" to "https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=800&auto=format&fit=crop&q=80",
        "Deine Beziehung abenteuerlich und spontan machen" to "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&auto=format&fit=crop&q=80",
        "Deinen besten Freund verlieren" to "https://images.unsplash.com/photo-1509114397022-ed747cca3f65?w=800&auto=format&fit=crop&q=80",
        "Alle deine Freunde verlieren, außer deinem besten Freund" to "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800&auto=format&fit=crop&q=80",
        "Teile alle deine Hobbys mit deinem Partner" to "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=800&auto=format&fit=crop&q=80",
        "Von deinem Partner in neue Hobbys eingeführt werden" to "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=80",
        "Derjenige sein, der umarmt wird" to "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80",
        "Diejenige sein, die umarmt" to "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80",
        "Verbringe die Feiertage mit deiner Familie" to "https://images.unsplash.com/photo-1512389142860-9c449e58a543?w=800&auto=format&fit=crop&q=80",
        "Die Feiertage mit der Familie deines Partners verbringen" to "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=800&auto=format&fit=crop&q=80",
        // Harmony premium local artwork: explicit one-image-per-option mappings.
        "Altbau mit Charme" to R.drawable.traumhaus_altbau,
        "Neubau mit Smart Home" to R.drawable.traumhaus_smart_home,
        "Offene Wohnküche" to R.drawable.traumhaus_wohnkueche,
        "Separate Küche" to R.drawable.traumhaus_separate_kueche,
        "Prasselnder Kamin" to R.drawable.traumhaus_kamin,
        "Fußbodenheizung" to R.drawable.traumhaus_fussbodenheizung,
        "Großer Garten" to R.drawable.traumhaus_garten,
        "Sonnige Dachterrasse" to R.drawable.traumhaus_dachterrasse,
        "Stadtvilla" to R.drawable.traumhaus_stadtvilla,
        "Landhaus" to R.drawable.traumhaus_landhaus,
        "Glasfassade" to R.drawable.traumhaus_glasfassade,
        "Natursteinfassade" to R.drawable.traumhaus_naturstein,
        "Penthouse mit Ausblick" to R.drawable.traumhaus_penthouse,
        "Haus am See" to R.drawable.traumhaus_see,
        "Minimalistisches Interieur" to R.drawable.traumhaus_minimal,
        "Landhausstil" to R.drawable.traumhaus_landhausstil,
        "Bibliothek" to R.drawable.traumhaus_bibliothek,
        "Heimkino" to R.drawable.traumhaus_heimkino,
        "Innenpool" to R.drawable.traumhaus_innenpool,
        "Wellnessbad" to R.drawable.traumhaus_wellnessbad,
        "Große Fensterfront" to R.drawable.traumhaus_fensterfront,
        "Privater Innenhof" to R.drawable.traumhaus_innenhof,
        "Tiny House" to R.drawable.traumhaus_tiny,
        "Mehrgenerationenhaus" to R.drawable.traumhaus_mehrgenerationenhaus,
        "Großer Außenpool" to R.drawable.aussen_pool,
        "Outdoor-Whirlpool" to R.drawable.aussen_whirlpool,
        "Moderne Grillstation" to R.drawable.aussen_grill,
        "Gemütliche Feuerstelle" to R.drawable.aussen_feuerstelle,
        "Eigenes Gemüsebeet" to R.drawable.aussen_gemuesebeet,
        "Bunte Blumenwiese" to R.drawable.aussen_blumenwiese,
        "Entspannte Hängematte" to R.drawable.aussen_haengematte,
        "Stilvolles Outdoor-Sofa" to R.drawable.aussen_sofa,
        "Infinity-Pool" to R.drawable.aussen_infinity,
        "Naturteich" to R.drawable.aussen_naturteich,
        "Outdoor-Küche" to R.drawable.aussen_outdoor_kueche,
        "Pizzaofen" to R.drawable.aussen_pizzaofen,
        "Pergola mit Lounge" to R.drawable.aussen_pergola,
        "Wintergarten" to R.drawable.aussen_wintergarten,
        "Kräuterbeet" to R.drawable.aussen_kraeuter,
        "Obstgarten" to R.drawable.aussen_obstgarten,
        "Dachgarten mit Lounge" to R.drawable.aussen_dachgarten,
        "Mediterraner Innenhof" to R.drawable.aussen_mediterraner_innenhof,
        "Feuerstelle" to R.drawable.aussen_feuerstelle_neu,
        "Außenkamin" to R.drawable.aussen_aussenkamin,
        "Spielbereich für Kinder" to R.drawable.aussen_spielbereich,
        "Sportplatz" to R.drawable.aussen_sportplatz,
        "Gewächshaus" to R.drawable.aussen_gewaechshaus,
        "Saunahaus" to R.drawable.aussen_saunahaus,
        "Klassisch Solitär" to R.drawable.ring_klassisch_solitaer,
        "Vintage verspielt" to R.drawable.ring_vintage_verspielt,
        "Gelbgold" to R.drawable.ring_gelbgold,
        "Weißgold" to R.drawable.ring_weissgold,
        "Großer Stein" to R.drawable.ring_grosser_stein,
        "Filigran & schlicht" to R.drawable.ring_filigran_schlicht,
        "Diamant" to R.drawable.ring_diamant,
        "Farbedelstein" to R.drawable.ring_farbedelstein,
        "Platin" to R.drawable.ring_platin,
        "Roségold" to R.drawable.ring_rosegold,
        "Drei-Stein-Ring" to R.drawable.ring_drei_stein,
        "Moderner Solitär" to R.drawable.ring_moderner_solitaer,
        "Ovaler Diamant" to R.drawable.ring_ovaler_diamant,
        "Runder Diamant" to R.drawable.ring_runder_diamant,
        "Schmal & zart" to R.drawable.ring_schmal_zart,
        "Markant & breit" to R.drawable.ring_markant_breit,
        "Moissanit" to R.drawable.ring_moissanit,
        "Saphir" to R.drawable.ring_saphir,
        "Vintage Art déco" to R.drawable.ring_art_deco,
        "Modern geometrisch" to R.drawable.ring_modern_geometrisch,
        "Gravur innen" to R.drawable.ring_gravur_innen,
        "Diamanten im Band" to R.drawable.ring_diamanten_band,
        "Ohne Stein" to R.drawable.ring_ohne_stein,
        "Statement-Ring" to R.drawable.ring_statement,
    )

    private val userOverrides = mutableMapOf<String, Any>()
    private val generatedOverrides = mutableMapOf<String, Any>()
    private val aliases = mutableMapOf<String, String>()

    init {
        // Register all shipped locales up front so every renderer — including
        // screens that only pass localized display text — resolves the same image.
        registerLocaleContent(EXACT_ENGLISH_CONTENT)
        registerLocaleContent(EXACT_ITALIAN_CONTENT)
        registerLocaleContent(EXACT_FRENCH_CONTENT)
        registerLocaleContent(EXACT_JAPANESE_CONTENT)
        registerLocaleContent(EXACT_SPANISH_LATIN_AMERICA_CONTENT)
        registerLocaleContent(EXACT_SPANISH_SPAIN_CONTENT)
        registerLocaleContent(EXACT_PORTUGUESE_BRAZIL_CONTENT)
        registerLocaleContent(EXACT_PORTUGUESE_PORTUGAL_CONTENT)
        registerLocaleContent(EXACT_PORTUGUESE_CONTENT)
        registerLocaleContent(EXACT_DANISH_CONTENT)
        registerLocaleContent(EXACT_NORWEGIAN_CONTENT)

        // Also ensure every directMap entry gets direct aliases for its translated terms
        directMap.keys.forEach { germanKey ->
            EXACT_ENGLISH_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_ITALIAN_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_FRENCH_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_JAPANESE_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_SPANISH_LATIN_AMERICA_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_SPANISH_SPAIN_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_PORTUGUESE_BRAZIL_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_PORTUGUESE_PORTUGAL_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_PORTUGUESE_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_DANISH_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
            EXACT_NORWEGIAN_CONTENT[germanKey]?.let { setAlias(it, germanKey) }
        }
    }

    /**
     * Registers all non-identical translation pairs as aliases back to the German key.
     */
    fun registerLocaleContent(map: Map<String, String>) {
        map.forEach { (source, localized) ->
            val sTrim = source.trim()
            val lTrim = localized.trim()
            if (sTrim.isNotEmpty() && lTrim.isNotEmpty() && !sTrim.equals(lTrim, ignoreCase = true)) {
                setAlias(lTrim, sTrim)
            }
        }
    }

    /**
     * Resolves a localized or alias text back to its canonical key (usually German).
     */
    fun resolveCanonicalKey(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        val lower = trimmed.lowercase()

        // 1. Check explicit alias
        aliases[trimmed]?.let { return it }
        aliases[lower]?.let { return it }

        // 2. Reverse lookup in translations
        aliases.entries.firstOrNull { it.key.equals(trimmed, ignoreCase = true) }?.value?.let { return it }
        EXACT_PORTUGUESE_BRAZIL_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_PORTUGUESE_PORTUGAL_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_PORTUGUESE_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_SPANISH_LATIN_AMERICA_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_SPANISH_SPAIN_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_FRENCH_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_JAPANESE_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_DANISH_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_NORWEGIAN_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_ITALIAN_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }
        EXACT_ENGLISH_CONTENT.entries.firstOrNull { it.value.equals(trimmed, ignoreCase = true) }?.key?.let { return it }

        return trimmed
    }

    /**
     * Resolves only an explicitly registered key and never returns the generic fallback.
     */
    private fun getExplicitImageOrNull(text: String, visited: Set<String> = emptySet()): Any? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()
        if (lower in visited) return null

        val canonical = resolveCanonicalKey(trimmed)
        if (canonical.isNotEmpty() && !canonical.equals(trimmed, ignoreCase = true)) {
            getExplicitImageOrNull(canonical, visited + lower)?.let { return it }
        }

        userOverrides[trimmed]?.let { return resolve(it) }
        userOverrides[lower]?.let { return resolve(it) }
        generatedOverrides[trimmed]?.let { return resolve(it) }
        generatedOverrides[lower]?.let { return resolve(it) }

        iceCreamImageKey(trimmed)?.let { key ->
            generatedOverrides[key]?.let { return resolve(it) }
            generatedOverrides[key.lowercase()]?.let { return resolve(it) }
            directMap[key]?.let { return it }
            directMap[key.lowercase()]?.let { return it }
        }

        directMap[trimmed]?.let { return it }
        directMap[lower]?.let { return it }

        if (canonical.isNotEmpty() && !canonical.equals(trimmed, ignoreCase = true)) {
            userOverrides[canonical]?.let { return resolve(it) }
            userOverrides[canonical.lowercase()]?.let { return resolve(it) }
            generatedOverrides[canonical]?.let { return resolve(it) }
            generatedOverrides[canonical.lowercase()]?.let { return resolve(it) }
            directMap[canonical]?.let { return it }
            directMap[canonical.lowercase()]?.let { return it }
        }

        return null
    }

    fun setAlias(aliasText: String, sourceText: String) {
        val aTrim = aliasText.trim()
        val sTrim = sourceText.trim()
        if (aTrim.isNotEmpty() && sTrim.isNotEmpty() && !aTrim.equals(sTrim, ignoreCase = true)) {
            aliases[aTrim] = sTrim
            aliases[aTrim.lowercase()] = sTrim
        }
    }

    fun setCustomImage(text: String, imageUriOrUrl: Any) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            userOverrides[trimmed] = imageUriOrUrl
            userOverrides[trimmed.lowercase()] = imageUriOrUrl
            version++
        }
    }

    fun removeCustomImage(text: String) {
        val trimmed = text.trim()
        userOverrides.remove(trimmed)
        userOverrides.remove(trimmed.lowercase())
        version++
    }

    fun setGeneratedImage(text: String, imageUriOrUrl: Any) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            generatedOverrides[trimmed] = imageUriOrUrl
            generatedOverrides[trimmed.lowercase()] = imageUriOrUrl
            version++
        }
    }

    fun clearGeneratedImages() {
        generatedOverrides.clear()
        version++
    }

    /** true, wenn für diesen Text ein eigenes Bild hinterlegt ist (nicht die Heuristik). */
    fun hasExplicitImage(text: String): Boolean {
        val t = text.trim()
        val l = t.lowercase()
        val canonical = resolveCanonicalKey(t)
        val cLower = canonical.lowercase()

        return userOverrides.containsKey(t) || userOverrides.containsKey(l) ||
                generatedOverrides.containsKey(t) || generatedOverrides.containsKey(l) ||
                directMap.containsKey(t) || directMap.containsKey(l) ||
                (canonical.isNotEmpty() && (
                    userOverrides.containsKey(canonical) || userOverrides.containsKey(cLower) ||
                    generatedOverrides.containsKey(canonical) || generatedOverrides.containsKey(cLower) ||
                    directMap.containsKey(canonical) || directMap.containsKey(cLower)
                ))
    }

    /** Macht aus einem gespeicherten Wert etwas, das Coil laden kann. */
    private fun resolve(value: Any): Any {
        if (value is String && value.startsWith("/")) return File(value)
        return value
    }

    private fun iceCreamImageKey(text: String): String? {
        val lower = text.trim().lowercase()
        return when {
            "vanille" in lower || "vanilla" in lower || "baunilha" in lower || "vaniglia" in lower -> "Vanille"
            "schokolade" in lower || "chocolate" in lower || "cioccolato" in lower -> "Schokolade"
            "erdbeer" in lower || "strawberry" in lower || "fragola" in lower || "morango" in lower -> "Erdbeere"
            "zitrone" in lower || "lemon" in lower || "limone" in lower || "limão" in lower || "limao" in lower -> "Zitrone"
            "stracciatella" in lower -> "Stracciatella"
            "pistaz" in lower || "pistachio" in lower || "pistacchio" in lower || "pistache" in lower -> "Pistazie"
            "mango" in lower || "manga" in lower -> "Mango Sorbet"
            "himbeer" in lower || "raspberry" in lower || "lampone" in lower || "framboesa" in lower -> "Himbeere"
            "salted caramel" in lower || "salzkaramell" in lower || "caramello salato" in lower || "caramelo salgado" in lower -> "Salted Caramel"
            "cookie dough" in lower || "impasto per biscotti" in lower || "massa de bolachas" in lower || "massa de biscoito" in lower || "biscott" in lower || "bolacha" in lower -> "Cookie Dough"
            "hazelnut" in lower || "haselnuss" in lower || "nocciola" in lower || "avelã" in lower || "avela" in lower -> "Hazelnut"
            "white chocolate" in lower || "weiße schokolade" in lower || "cioccolato bianco" in lower || "chocolate branco" in lower -> "White Chocolate"
            "walnuss" in lower || "walnut" in lower || "noce" in lower || "noz" in lower -> "Walnuss"
            "banane" in lower || "banana" in lower -> "Banane"
            else -> null
        }
    }

    fun getImageUrl(text: String): Any {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
        }
        val lower = trimmed.lowercase()

        // 0. Explizite Abfrage (inkl. Alias-Auflösung und direkter Maps)
        getExplicitImageOrNull(trimmed)?.let { return it }

        // 1. Kanonischer Schlüssel
        val canonical = resolveCanonicalKey(trimmed)
        if (canonical.isNotEmpty() && !canonical.equals(trimmed, ignoreCase = true)) {
            getExplicitImageOrNull(canonical)?.let { return it }
        }

        // 2. Fuzzy match auf generierte Overrides
        generatedOverrides.entries.firstOrNull { (key, _) ->
            val can = key.trim().lowercase()
            can.length >= 4 && (lower.contains(can) || can.contains(lower))
        }?.value?.let { return resolve(it) }

        // 3. Multilinguale Stichwort-Heuristik
        return getHeuristicFallback(lower, canonical.lowercase())
    }

    private fun getHeuristicFallback(lower: String, canonicalLower: String): Any {
        val combined = "$lower $canonicalLower"
        return when {
            "tokyo" in combined || "japan" in combined || "giappone" in combined || "japão" in combined || "japao" in combined -> R.drawable.tokyo_tower_zojoji
            "seychellen" in combined || "seychelles" in combined || "seicelle" in combined -> "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&auto=format&fit=crop&q=80"
            "malediven" in combined || "maldives" in combined || "maldive" in combined || "maldivas" in combined -> "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=800&auto=format&fit=crop&q=80"
            "gewitter" in combined || "regen" in combined || "sturm" in combined || "thunderstorm" in combined || "rain" in combined || "storm" in combined || "temporale" in combined || "pioggia" in combined || "tempestade" in combined || "chuva" in combined -> "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=800&auto=format&fit=crop&q=80"
            "mond" in combined || "stern" in combined || "nacht" in combined || "moon" in combined || "star" in combined || "night" in combined || "notte" in combined || "luna" in combined || "stelle" in combined || "lua" in combined || "noite" in combined || "estrelas" in combined -> "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&auto=format&fit=crop&q=80"
            "lego" in combined -> "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=800&auto=format&fit=crop&q=80"
            "schokolade" in combined || "chocolate" in combined || "cioccolato" in combined -> "https://images.unsplash.com/photo-1511381939415-e44015466834?w=800&auto=format&fit=crop&q=80"
            "picknick" in combined || "picnic" in combined || "piquenique" in combined -> "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=800&auto=format&fit=crop&q=80"
            "grill" in combined || "barbecue" in combined || "churrasco" in combined || "grigliata" in combined -> "https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?w=800&auto=format&fit=crop&q=80"
            "feuerstelle" in combined || "lagerfeuer" in combined || "campfire" in combined || "fire pit" in combined || "focolare" in combined || "fogueira" in combined || "lareira" in combined -> "https://images.unsplash.com/photo-1517824806704-9040b037703b?w=800&auto=format&fit=crop&q=80"
            "gemüse" in combined || "beet" in combined || "vegetable" in combined || "garden bed" in combined || "verdure" in combined || "legumes" in combined || "horta" in combined || "orto" in combined -> "https://images.unsplash.com/photo-1592417817098-8f3d6eb12765?w=800&auto=format&fit=crop&q=80"
            "blumenwiese" in combined || "wiese" in combined || "meadow" in combined || "flower" in combined || "prato" in combined || "fiori" in combined || "flores" in combined -> "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=800&auto=format&fit=crop&q=80"
            "hängematte" in combined || "hammock" in combined || "amaca" in combined || "rede" in combined -> "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80"
            "sofa" in combined || "lounge" in combined || "divano" in combined || "sofá" in combined -> "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=800&auto=format&fit=crop&q=80"
            "whirlpool" in combined || "jacuzzi" in combined || "hot tub" in combined || "hottub" in combined || "hidromassagem" in combined || "idromassaggio" in combined -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80"
            "pool" in combined || "piscina" in combined || "swimming" in combined -> "https://images.unsplash.com/photo-1576013551627-0cc20b96c2a7?w=800&auto=format&fit=crop&q=80"
            "spa" in combined || "wellness" in combined || "terme" in combined || "benessere" in combined || "bem-estar" in combined -> "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800&auto=format&fit=crop&q=80"
            "wein" in combined || "sekt" in combined || "wine" in combined || "champagne" in combined || "vino" in combined || "vinho" in combined -> "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=800&auto=format&fit=crop&q=80"
            "boot" in combined || "yacht" in combined || "boat" in combined || "barca" in combined || "barco" in combined -> "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&auto=format&fit=crop&q=80"
            "sonnenaufgang" in combined || "morgen" in combined || "sunrise" in combined || "morning" in combined || "alba" in combined || "mattino" in combined || "nascer do sol" in combined || "amanhecer" in combined -> "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=800&auto=format&fit=crop&q=80"
            "sonnenuntergang" in combined || "abend" in combined || "sunset" in combined || "evening" in combined || "tramonto" in combined || "sera" in combined || "pôr do sol" in combined || "entardecer" in combined -> "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?w=800&auto=format&fit=crop&q=80"
            "spiel" in combined || "gaming" in combined || "board" in combined || "game" in combined || "gioco" in combined || "jogo" in combined -> "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800&auto=format&fit=crop&q=80"
            "million" in combined || "dollar" in combined || "geld" in combined || "money" in combined || "soldi" in combined || "dinheiro" in combined -> "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=80"
            "geheimnis" in combined || "vertrauen" in combined || "secret" in combined || "trust" in combined || "segreto" in combined || "fiducia" in combined || "segredo" in combined || "confiança" in combined || "confianca" in combined -> "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&auto=format&fit=crop&q=80"
            "umarm" in combined || "hug" in combined || "abbraccio" in combined || "abraço" in combined || "abraco" in combined -> "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
            "fernbeziehung" in combined || "telefon" in combined || "long distance" in combined || "relazione a distanza" in combined || "relacionamento à distância" in combined || "relacionamento a distancia" in combined -> "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=80"
            "familie" in combined || "feiertag" in combined || "family" in combined || "holiday" in combined || "famiglia" in combined || "festa" in combined || "família" in combined || "familia" in combined || "feriado" in combined -> "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=800&auto=format&fit=crop&q=80"
            "auto" in combined || "roadtrip" in combined || "reise" in combined || "trip" in combined || "travel" in combined || "viaggio" in combined || "viagem" in combined || "carro" in combined -> "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&auto=format&fit=crop&q=80"
            "tanz" in combined || "tanzen" in combined || "dance" in combined || "ballo" in combined || "dança" in combined || "danca" in combined -> "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=800&auto=format&fit=crop&q=80"
            "zelten" in combined || "camp" in combined || "camping" in combined || "campeggio" in combined || "acampamento" in combined || "acampar" in combined -> "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80"
            "feuer" in combined || "kamin" in combined || "fireplace" in combined || "camino" in combined || "fogo" in combined -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80"
            "küche" in combined || "kochen" in combined || "kitchen" in combined || "cook" in combined || "cucina" in combined || "cozinha" in combined || "culinária" in combined || "culinaria" in combined -> "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&auto=format&fit=crop&q=80"
            "garten" in combined || "pflanze" in combined || "blume" in combined || "garden" in combined || "plant" in combined || "flower" in combined || "giardino" in combined || "pianta" in combined || "jardim" in combined || "flor" in combined -> "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=800&auto=format&fit=crop&q=80"
            "strand" in combined || "meer" in combined || "ozean" in combined || "beach" in combined || "sea" in combined || "ocean" in combined || "spiaggia" in combined || "mare" in combined || "oceano" in combined || "praia" in combined -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80"
            "berg" in combined || "wander" in combined || "mountain" in combined || "hike" in combined || "montagna" in combined || "montanha" in combined || "caminhada" in combined || "trilha" in combined -> "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80"
            "hochzeit" in combined || "braut" in combined || "wedding" in combined || "bride" in combined || "matrimonio" in combined || "sposa" in combined || "casamento" in combined || "noiva" in combined -> "https://images.unsplash.com/photo-1519741497674-611481863552?w=800&auto=format&fit=crop&q=80"
            "ring" in combined || "diamant" in combined || "gold" in combined || "diamond" in combined || "anello" in combined || "diamante" in combined || "ouro" in combined || "anel" in combined -> "https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=800&auto=format&fit=crop&q=80"
            "essen" in combined || "restaurant" in combined || "diner" in combined || "food" in combined || "mangiare" in combined || "comida" in combined || "ristorante" in combined || "restaurante" in combined -> "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80"
            "film" in combined || "kino" in combined || "serie" in combined || "movie" in combined || "cinema" in combined -> "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80"
            "musik" in combined || "konzert" in combined || "party" in combined || "club" in combined || "concert" in combined || "musica" in combined || "concerto" in combined -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80"
            "stadt" in combined || "flug" in combined || "city" in combined || "flight" in combined || "città" in combined || "citta" in combined || "volo" in combined || "cidade" in combined || "voo" in combined -> "https://images.unsplash.com/photo-1477959858617-67f30ac4ce78?w=800&auto=format&fit=crop&q=80"
            "haus" in combined || "wohnung" in combined || "house" in combined || "home" in combined || "apartment" in combined || "casa" in combined || "appartamento" in combined -> "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&auto=format&fit=crop&q=80"
            "freund" in combined || "liebe" in combined || "paar" in combined || "friend" in combined || "love" in combined || "couple" in combined || "amico" in combined || "amore" in combined || "coppia" in combined || "amigo" in combined || "amor" in combined || "casal" in combined -> "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
            else -> "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
        }
    }

    /**
     * Language-safe image lookup.
     *
     * New content can register an image under [assetKey]. Existing German-keyed images
     * continue to work through [legacyAssetKey]. Both assetKey and localized text are checked.
     */
    fun getImageUrl(assetKey: String, legacyAssetKey: String): Any {
        if (assetKey.isNotBlank()) {
            getExplicitImageOrNull(assetKey)?.let { return it }
        }
        if (legacyAssetKey.isNotBlank()) {
            getExplicitImageOrNull(legacyAssetKey)?.let { return it }
        }
        if (assetKey.isNotBlank()) {
            return getImageUrl(assetKey)
        }
        return getImageUrl(legacyAssetKey)
    }
}
