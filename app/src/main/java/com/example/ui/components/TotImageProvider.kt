package com.example.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.R
import com.example.ui.EXACT_ENGLISH_CONTENT
import com.example.ui.EXACT_FRENCH_CONTENT
import com.example.ui.EXACT_ITALIAN_CONTENT
import com.example.ui.EXACT_JAPANESE_CONTENT
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

        // ★ Cucina italiana — scelte regionali
        // These keys use the pack id + pair index + side. They intentionally do not use
        // the visible Italian label, so every card keeps its intended splash art.
        "tot:tot_italian_cuisine_mixed:0:a" to R.drawable.it_01_pizza_napoletana,
        "tot:tot_italian_cuisine_mixed:0:b" to R.drawable.it_01_calzone,
        "tot:tot_italian_cuisine_mixed:1:a" to R.drawable.vespucci_02_sformatino_zucchine_pecorino,
        "tot:tot_italian_cuisine_mixed:1:b" to R.drawable.vespucci_02_pappa_al_pomodoro,
        "tot:tot_italian_cuisine_mixed:2:a" to R.drawable.it_17_spaghetti_alle_vongole,
        "tot:tot_italian_cuisine_mixed:2:b" to R.drawable.it_17_fritto_misto_di_mare,
        "tot:tot_italian_cuisine_mixed:3:a" to R.drawable.it_02_carbonara,
        "tot:tot_italian_cuisine_mixed:3:b" to R.drawable.it_02_amatriciana,
        "tot:tot_italian_cuisine_mixed:4:a" to R.drawable.it_16_pasta_alla_norma,
        "tot:tot_italian_cuisine_mixed:4:b" to R.drawable.it_16_risotto_ai_funghi,
        "tot:tot_italian_cuisine_mixed:5:a" to R.drawable.vespucci_03_pappardelle_al_cinghiale,
        "tot:tot_italian_cuisine_mixed:5:b" to R.drawable.vespucci_03_pici_senesi_ragu_chianina,
        "tot:tot_italian_cuisine_mixed:6:a" to R.drawable.it_03_lasagne_alla_bolognese,
        "tot:tot_italian_cuisine_mixed:6:b" to R.drawable.it_03_cannelloni,
        "tot:tot_italian_cuisine_mixed:7:a" to R.drawable.it_19_cacciucco,
        "tot:tot_italian_cuisine_mixed:7:b" to R.drawable.it_19_brodetto_di_pesce,
        "tot:tot_italian_cuisine_mixed:8:a" to R.drawable.it_04_risotto_alla_milanese,
        "tot:tot_italian_cuisine_mixed:8:b" to R.drawable.it_04_polenta,
        "tot:tot_italian_cuisine_mixed:9:a" to R.drawable.it_05_arancini,
        "tot:tot_italian_cuisine_mixed:9:b" to R.drawable.it_05_suppli,
        "tot:tot_italian_cuisine_mixed:10:a" to R.drawable.it_18_ribollita,
        "tot:tot_italian_cuisine_mixed:10:b" to R.drawable.it_18_panzanella,
        "tot:tot_italian_cuisine_mixed:11:a" to R.drawable.vespucci_01_bruschetta_pomodoro_basilico,
        "tot:tot_italian_cuisine_mixed:11:b" to R.drawable.vespucci_01_crostini_toscani_fegato_pollo,
        "tot:tot_italian_cuisine_mixed:12:a" to R.drawable.it_06_pesto_alla_genovese,
        "tot:tot_italian_cuisine_mixed:12:b" to R.drawable.it_06_ragu_alla_bolognese,
        "tot:tot_italian_cuisine_mixed:13:a" to R.drawable.it_21_baccala_mantecato,
        "tot:tot_italian_cuisine_mixed:13:b" to R.drawable.it_21_sarde_in_saor,
        "tot:tot_italian_cuisine_mixed:14:a" to R.drawable.it_07_orecchiette_alle_cime_di_rapa,
        "tot:tot_italian_cuisine_mixed:14:b" to R.drawable.it_07_trofie_al_pesto,
        "tot:tot_italian_cuisine_mixed:15:a" to R.drawable.it_20_insalata_caprese,
        "tot:tot_italian_cuisine_mixed:15:b" to R.drawable.it_20_fiori_di_zucca_ripieni,
        "tot:tot_italian_cuisine_mixed:16:a" to R.drawable.it_08_parmigiana_di_melanzane,
        "tot:tot_italian_cuisine_mixed:16:b" to R.drawable.it_08_caponata,
        "tot:tot_italian_cuisine_mixed:17:a" to R.drawable.vespucci_04_trippa_alla_fiorentina,
        "tot:tot_italian_cuisine_mixed:17:b" to R.drawable.vespucci_04_peposo_all_impruneta,
        "tot:tot_italian_cuisine_mixed:18:a" to R.drawable.it_23_risotto_alla_pescatora,
        "tot:tot_italian_cuisine_mixed:18:b" to R.drawable.it_23_orata_al_cartoccio,
        "tot:tot_italian_cuisine_mixed:19:a" to R.drawable.it_09_ossobuco,
        "tot:tot_italian_cuisine_mixed:19:b" to R.drawable.it_09_saltimbocca_alla_romana,
        "tot:tot_italian_cuisine_mixed:20:a" to R.drawable.it_22_gnocchi_alla_sorrentina,
        "tot:tot_italian_cuisine_mixed:20:b" to R.drawable.it_22_pasta_e_fagioli,
        "tot:tot_italian_cuisine_mixed:21:a" to R.drawable.it_10_bistecca_alla_fiorentina,
        "tot:tot_italian_cuisine_mixed:21:b" to R.drawable.it_10_arrosticini,
        "tot:tot_italian_cuisine_mixed:22:a" to R.drawable.it_25_insalata_di_polpo,
        "tot:tot_italian_cuisine_mixed:22:b" to R.drawable.it_25_spaghetti_allo_scoglio,
        "tot:tot_italian_cuisine_mixed:23:a" to R.drawable.it_11_focaccia_genovese,
        "tot:tot_italian_cuisine_mixed:23:b" to R.drawable.it_11_piadina_romagnola,
        "tot:tot_italian_cuisine_mixed:24:a" to R.drawable.it_24_carciofi_alla_romana,
        "tot:tot_italian_cuisine_mixed:24:b" to R.drawable.it_24_polenta_ai_funghi,
        "tot:tot_italian_cuisine_mixed:25:a" to R.drawable.it_12_gnocchi,
        "tot:tot_italian_cuisine_mixed:25:b" to R.drawable.it_12_ravioli,
        "tot:tot_italian_cuisine_mixed:26:a" to R.drawable.vespucci_05_cantucci_vin_santo,
        "tot:tot_italian_cuisine_mixed:26:b" to R.drawable.vespucci_05_tortino_cioccolato_cuore_caldo,
        "tot:tot_italian_cuisine_mixed:27:a" to R.drawable.it_13_tiramisu,
        "tot:tot_italian_cuisine_mixed:27:b" to R.drawable.it_13_panna_cotta,
        "tot:tot_italian_cuisine_mixed:28:a" to R.drawable.it_14_cannoli_siciliani,
        "tot:tot_italian_cuisine_mixed:28:b" to R.drawable.it_14_sfogliatella,
        "tot:tot_italian_cuisine_mixed:29:a" to R.drawable.it_15_gelato,
        "tot:tot_italian_cuisine_mixed:29:b" to R.drawable.it_15_semifreddo,

        // ★ Tradycyjna kuchnia polska
        // Stable keys decouple splash art from visible Polish labels and translations.
        "tot:tot_polish_cuisine_traditional:0:a" to R.drawable.pl_01_pierogi_ruskie,
        "tot:tot_polish_cuisine_traditional:0:b" to R.drawable.pl_01_bigos,
        "tot:tot_polish_cuisine_traditional:1:a" to R.drawable.pl_02_zurek,
        "tot:tot_polish_cuisine_traditional:1:b" to R.drawable.pl_02_barszcz_czerwony,
        "tot:tot_polish_cuisine_traditional:2:a" to R.drawable.pl_03_kotlet_schabowy,
        "tot:tot_polish_cuisine_traditional:2:b" to R.drawable.pl_03_placki_ziemniaczane,
        "tot:tot_polish_cuisine_traditional:3:a" to R.drawable.pl_04_golabki,
        "tot:tot_polish_cuisine_traditional:3:b" to R.drawable.pl_04_kopytka,
        "tot:tot_polish_cuisine_traditional:4:a" to R.drawable.pl_05_rosol,
        "tot:tot_polish_cuisine_traditional:4:b" to R.drawable.pl_05_flaki,
        "tot:tot_polish_cuisine_traditional:5:a" to R.drawable.pl_06_zeberka_w_kapuscie,
        "tot:tot_polish_cuisine_traditional:5:b" to R.drawable.pl_06_kaszanka,
        "tot:tot_polish_cuisine_traditional:6:a" to R.drawable.pl_07_lazanki,
        "tot:tot_polish_cuisine_traditional:6:b" to R.drawable.pl_07_zrazy_wolowe,
        "tot:tot_polish_cuisine_traditional:7:a" to R.drawable.pl_08_kaczka_po_poznansku,
        "tot:tot_polish_cuisine_traditional:7:b" to R.drawable.pl_08_gulasz,
        "tot:tot_polish_cuisine_traditional:8:a" to R.drawable.pl_09_ryba_po_grecku,
        "tot:tot_polish_cuisine_traditional:8:b" to R.drawable.pl_09_sledz_w_smietanie,
        "tot:tot_polish_cuisine_traditional:9:a" to R.drawable.pl_10_placki_po_wegiersku,
        "tot:tot_polish_cuisine_traditional:9:b" to R.drawable.pl_10_racuchy,
        "tot:tot_polish_cuisine_traditional:10:a" to R.drawable.pl_11_pyzy_ziemniaczane,
        "tot:tot_polish_cuisine_traditional:10:b" to R.drawable.pl_11_kartacze,
        "tot:tot_polish_cuisine_traditional:11:a" to R.drawable.pl_12_oscypek_z_zurawina,
        "tot:tot_polish_cuisine_traditional:11:b" to R.drawable.pl_12_bryndza,
        "tot:tot_polish_cuisine_traditional:12:a" to R.drawable.pl_13_makowiec,
        "tot:tot_polish_cuisine_traditional:12:b" to R.drawable.pl_13_sernik,
        "tot:tot_polish_cuisine_traditional:13:a" to R.drawable.pl_14_szarlotka,
        "tot:tot_polish_cuisine_traditional:13:b" to R.drawable.pl_14_paczki,
    )

    private val userOverrides = mutableMapOf<String, Any>()
    private val generatedOverrides = mutableMapOf<String, Any>()
    private val aliases = mutableMapOf<String, String>()

    init {
        // Option labels are localized for display, but image resources are keyed by
        // their canonical German source. Register every shipped locale up front so
        // every renderer — including legacy screens that only pass display text —
        // resolves the same image after a language switch.
        (
            EXACT_ENGLISH_CONTENT.entries +
                EXACT_FRENCH_CONTENT.entries +
                EXACT_ITALIAN_CONTENT.entries +
                EXACT_JAPANESE_CONTENT.entries +
                EXACT_SPANISH_LATIN_AMERICA_CONTENT.entries +
                EXACT_SPANISH_SPAIN_CONTENT.entries
        ).forEach { (source, localized) ->
            if (!source.equals(localized, ignoreCase = true)) {
                setAlias(localized, source)
            }
        }
    }

    /**
     * Resolves only an explicitly registered key and never returns the generic fallback.
     * This lets a stable, language-independent asset key be attempted before a legacy
     * display-text key without mistaking the fallback image for a successful lookup.
     */
    private fun getExplicitImageOrNull(text: String, visited: Set<String> = emptySet()): Any? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()
        if (lower in visited) return null

        val targetText = aliases[trimmed] ?: aliases[lower]
        if (targetText != null && !targetText.equals(trimmed, ignoreCase = true)) {
            getExplicitImageOrNull(targetText, visited + lower)?.let { return it }
        }

        userOverrides[trimmed]?.let { return resolve(it) }
        userOverrides[lower]?.let { return resolve(it) }
        generatedOverrides[trimmed]?.let { return resolve(it) }
        generatedOverrides[lower]?.let { return resolve(it) }
        directMap[trimmed]?.let { return it }
        return null
    }

    fun setAlias(aliasText: String, sourceText: String) {
        val aTrim = aliasText.trim()
        val sTrim = sourceText.trim()
        if (aTrim.isNotEmpty() && sTrim.isNotEmpty()) {
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
        return userOverrides.containsKey(t) || userOverrides.containsKey(l) ||
                generatedOverrides.containsKey(t) || generatedOverrides.containsKey(l) ||
                directMap.containsKey(t)
    }

    /** Macht aus einem gespeicherten Wert etwas, das Coil laden kann. */
    private fun resolve(value: Any): Any {
        if (value is String && value.startsWith("/")) return File(value)
        return value
    }

    private fun iceCreamImageKey(text: String): String? {
        val lower = text.trim().lowercase()
        return when {
            "vanille" in lower -> "Vanille"
            "schokolade" in lower || "chocolate" in lower -> "Schokolade"
            "erdbeer" in lower -> "Erdbeere"
            "zitrone" in lower || "lemon" in lower -> "Zitrone"
            "stracciatella" in lower -> "Stracciatella"
            "pistaz" in lower -> "Pistazie"
            "mango" in lower -> "Mango Sorbet"
            "himbeer" in lower || "raspberry" in lower -> "Himbeere"
            "salted caramel" in lower || "salzkaramell" in lower -> "Salted Caramel"
            "cookie dough" in lower -> "Cookie Dough"
            "hazelnut" in lower || "haselnuss" in lower -> "Hazelnut"
            "white chocolate" in lower || "weiße schokolade" in lower -> "White Chocolate"
            "walnuss" in lower -> "Walnuss"
            "banane" in lower || "banana" in lower -> "Banane"
            else -> null
        }
    }

    fun getImageUrl(text: String): Any {
        val trimmed = text.trim()
        val lower = trimmed.lowercase()

        // 0. Alias prüfen (z.B. "1 Jahr lang in Rom" -> "Rom")
        val targetText = aliases[trimmed] ?: aliases[lower]
        if (targetText != null && targetText != trimmed) {
            return getImageUrl(targetText)
        }

        // 1. Vom Entwickler gesetzt
        userOverrides[trimmed]?.let { return resolve(it) }
        userOverrides[lower]?.let { return resolve(it) }

        // 2. Aus dem generierten Content
        generatedOverrides[trimmed]?.let { return resolve(it) }
        generatedOverrides[lower]?.let { return resolve(it) }

        // 3. Eingebettete/generated Bilder: robuste Sorten-Aliase.
        // Der Content darf z.B. "Vanille Bourbon" oder "Belgische Schokolade"
        // anzeigen, während das Bild unter "Vanille" bzw. "Schokolade" gespeichert ist.
        iceCreamImageKey(trimmed)?.let { key ->
            generatedOverrides[key]?.let { return resolve(it) }
            generatedOverrides[key.lowercase()]?.let { return resolve(it) }
        }
        generatedOverrides.entries.firstOrNull { (key, _) ->
            val canonical = key.trim().lowercase()
            canonical.length >= 4 && (lower.contains(canonical) || canonical.contains(lower))
        }?.value?.let { return resolve(it) }

        // 4. Fest eingebaut
        directMap[text]?.let { return it }
        directMap[trimmed]?.let { return it }

        return when {
            "tokyo" in lower || "japan" in lower -> R.drawable.tokyo_tower_zojoji
            "seychellen" in lower -> "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&auto=format&fit=crop&q=80"
            "malediven" in lower -> "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=800&auto=format&fit=crop&q=80"
            "gewitter" in lower || "regen" in lower || "sturm" in lower -> "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=800&auto=format&fit=crop&q=80"
            "mond" in lower || "stern" in lower || "nacht" in lower -> "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&auto=format&fit=crop&q=80"
            "lego" in lower -> "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=800&auto=format&fit=crop&q=80"
            "schokolade" in lower -> "https://images.unsplash.com/photo-1511381939415-e44015466834?w=800&auto=format&fit=crop&q=80"
            "picknick" in lower -> "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=800&auto=format&fit=crop&q=80"
            "grill" in lower -> "https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?w=800&auto=format&fit=crop&q=80"
            "feuerstelle" in lower || "lagerfeuer" in lower -> "https://images.unsplash.com/photo-1517824806704-9040b037703b?w=800&auto=format&fit=crop&q=80"
            "gemüse" in lower || "beet" in lower -> "https://images.unsplash.com/photo-1592417817098-8f3d6eb12765?w=800&auto=format&fit=crop&q=80"
            "blumenwiese" in lower || "wiese" in lower -> "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=800&auto=format&fit=crop&q=80"
            "hängematte" in lower -> "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80"
            "sofa" in lower || "lounge" in lower -> "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=800&auto=format&fit=crop&q=80"
            "whirlpool" in lower || "jacuzzi" in lower || "hot tub" in lower || "hottub" in lower -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80"
            "pool" in lower -> "https://images.unsplash.com/photo-1576013551627-0cc20b96c2a7?w=800&auto=format&fit=crop&q=80"
            "spa" in lower || "wellness" in lower -> "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800&auto=format&fit=crop&q=80"
            "wein" in lower || "sekt" in lower -> "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=800&auto=format&fit=crop&q=80"
            "boot" in lower || "yacht" in lower -> "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&auto=format&fit=crop&q=80"
            "sonnenaufgang" in lower || "morgen" in lower -> "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=800&auto=format&fit=crop&q=80"
            "sonnenuntergang" in lower || "abend" in lower -> "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?w=800&auto=format&fit=crop&q=80"
            "spiel" in lower || "gaming" in lower || "board" in lower -> "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800&auto=format&fit=crop&q=80"
            "million" in lower || "dollar" in lower || "geld" in lower -> "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=80"
            "geheimnis" in lower || "vertrauen" in lower -> "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&auto=format&fit=crop&q=80"
            "umarm" in lower -> "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
            "fernbeziehung" in lower || "telefon" in lower -> "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=80"
            "familie" in lower || "feiertag" in lower -> "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=800&auto=format&fit=crop&q=80"
            "auto" in lower || "roadtrip" in lower || "reise" in lower -> "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&auto=format&fit=crop&q=80"
            "tanz" in lower || "tanzen" in lower -> "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=800&auto=format&fit=crop&q=80"
            "zelten" in lower || "camp" in lower -> "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80"
            "feuer" in lower || "kamin" in lower -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80"
            "küche" in lower || "kochen" in lower -> "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&auto=format&fit=crop&q=80"
            "garten" in lower || "pflanze" in lower || "blume" in lower -> "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=800&auto=format&fit=crop&q=80"
            "strand" in lower || "meer" in lower || "ozean" in lower -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80"
            "berg" in lower || "wander" in lower -> "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80"
            "hochzeit" in lower || "braut" in lower -> "https://images.unsplash.com/photo-1519741497674-611481863552?w=800&auto=format&fit=crop&q=80"
            "ring" in lower || "diamant" in lower || "gold" in lower -> "https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=800&auto=format&fit=crop&q=80"
            "essen" in lower || "restaurant" in lower || "diner" in lower -> "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80"
            "film" in lower || "kino" in lower || "serie" in lower -> "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80"
            "musik" in lower || "konzert" in lower || "party" in lower || "club" in lower -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80"
            "stadt" in lower || "flug" in lower -> "https://images.unsplash.com/photo-1477959858617-67f30ac4ce78?w=800&auto=format&fit=crop&q=80"
            "haus" in lower || "wohnung" in lower -> "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&auto=format&fit=crop&q=80"
            "freund" in lower || "liebe" in lower || "paar" in lower -> "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
            else -> "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800&auto=format&fit=crop&q=80"
        }
    }

    /**
     * Language-safe image lookup.
     *
     * New content can register an image under [assetKey]. Existing German-keyed images
     * continue to work through [legacyAssetKey]. The localized label is deliberately not
     * accepted here, so switching to English or any future locale cannot change the image.
     */
    fun getImageUrl(assetKey: String, legacyAssetKey: String): Any {
        getExplicitImageOrNull(assetKey)?.let { return it }
        return getImageUrl(legacyAssetKey)
    }
}
