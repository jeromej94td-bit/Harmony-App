package com.example.data

import android.content.Context
import android.util.Base64
import com.example.data.model.HarmonyPacksData
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Installs the bundled "Das oder das?" image assets that ship inside the APK.
 *
 * Existing Drive-backed images stay in drive_tot_assets.zip. The generated
 * "Marken & Alltag" images are stored as a split Base64 bundle so they can live
 * in the repository as normal text assets. Both bundles are extracted to
 * app-private storage and registered as generated images by HarmonyViewModel.
 *
 * The additional Marken & Alltag pairs are intentionally text-only here.
 */
object DriveTotAssetInstaller {
    private const val DRIVE_ASSET_ZIP = "drive_tot_assets.zip"
    private const val OUTPUT_DIR = "drive_tot_assets_v2"

    private val BRAND_ASSET_CHUNKS = listOf(
        "brand_everyday_assets_01.b64",
        "brand_everyday_assets_02.b64",
        "brand_everyday_assets_03_04.b64",
        "brand_everyday_assets_05_06.b64",
        "brand_everyday_assets_07_08.b64"
    )

    private val driveOptionToFile = linkedMapOf(
        // Getränke
        "Cappuccino" to "drink_cappuccino.webp",
        "Matcha-Latte" to "drink_matcha_latte.webp",
        "Heiße Schokolade" to "drink_heisse_schokolade.webp",
        "Eistee" to "drink_schwarzer_eistee.webp",
        "Minzlimonade" to "drink_minzlimonade.webp",
        "Fruchtpunsch" to "drink_fruchtpunsch.webp",
        "Bier" to "drink_bier.webp",
        "Rote-Bete-Saft" to "drink_rote_bete_saft.webp",
        "Coca-Cola" to "drink_coca_cola.webp",
        "Fanta" to "drink_fanta.webp",
        "Orangensaft" to "drink_orangensaft.webp",
        "Apfelsaft" to "drink_apfelsaft.webp",
        "Kaffee" to "drink_kaffee.webp",
        "Tee" to "drink_tee.webp",

        // Tiere
        "Hund" to "animal_hund.webp",
        "Katze" to "animal_katze.webp",
        "Singvogel" to "animal_singvogel.webp",
        "Pinguin" to "animal_pinguin.webp",
        "Kaninchen" to "animal_kaninchen.webp",
        "Otter" to "animal_otter.webp",
        "Roter Panda" to "animal_roter_panda.webp",
        "Fuchs" to "animal_fuchs.webp",
        "Meerschweinchen" to "animal_meerschweinchen.webp",
        "Giraffe" to "animal_giraffe.webp",
        "Löwe" to "animal_loewe.webp",
        "Gorilla" to "animal_gorilla.webp",
        "Meeresschildkröte" to "animal_meeresschildkroete.webp",
        "Igel" to "animal_igel.webp",
        "Tiger" to "animal_tiger.webp",
        "Wolf" to "animal_wolf.webp",
        "Adler" to "animal_adler.webp",
        "Delfin" to "animal_delfin.webp",

        // Aktivitäten & Hobbys
        "Töpfern" to "hobby_toepfern.webp",
        "Klavier spielen" to "hobby_klavier.webp",
        "Malen" to "hobby_malen.webp",
        "Zeichnen" to "hobby_zeichnen.webp",
        "Badminton" to "hobby_badminton.webp",
        "Mountainbike" to "hobby_mountainbike.webp",
        "Bowling" to "hobby_bowling.webp",
        "Holzwerken" to "hobby_holzwerken.webp",
        "Gitarre spielen" to "hobby_gitarre.webp",
        "Tennis" to "hobby_tennis.webp",
        "Brettspiele" to "hobby_brettspiele.webp",
        "Darts" to "hobby_darts.webp",

        // Reiseziele. Drive labels were removed before bundling.
        "Miami, USA" to "travel_miami.webp",
        "Bangkok, Thailand" to "travel_bangkok.webp",
        "Chicago, USA" to "travel_chicago.webp",
        "Barcelona, Spanien" to "travel_barcelona.webp",
        "Lissabon, Portugal" to "travel_lissabon.webp",
        "Kopenhagen, Dänemark" to "travel_kopenhagen.webp",
        "Prag, Tschechien" to "travel_prag.webp",
        "Budapest, Ungarn" to "travel_budapest.webp",
        "Tokyo, Japan" to "travel_tokyo.webp"
    )

    private val brandOptionToFile = linkedMapOf(
        "McDonald’s" to "brand_mcdonalds.webp",
        "Burger King" to "brand_burger_king.webp",
        "iPhone" to "brand_iphone.webp",
        "Android" to "brand_android.webp",
        "Netflix" to "brand_netflix.webp",
        "Kino" to "brand_kino.webp",
        "Nike" to "brand_nike.webp",
        "Adidas" to "brand_adidas.webp",
        "Spotify" to "brand_spotify.webp",
        "YouTube Music" to "brand_youtube_music.webp",
        "PlayStation" to "brand_playstation.webp",
        "Xbox" to "brand_xbox.webp",
        "Coca-Cola" to "brand_coca_cola.webp",
        "Pepsi" to "brand_pepsi.webp",
        "IKEA" to "brand_ikea.webp",
        "Möbelhaus" to "brand_moebelhaus.webp",
        "Amazon" to "brand_amazon.webp",
        "Lokal einkaufen" to "brand_lokal_einkaufen.webp",
        "Disney" to "brand_disney.webp",
        "Studio Ghibli" to "brand_studio_ghibli.webp"
    )

    private val extraBrandPairs = listOf(
        "Starbucks" to "Dunkin’",
        "Red Bull" to "Monster Energy",
        "Nutella" to "Lotus Biscoff",
        "Haribo" to "Trolli",
        "Pringles" to "Doritos",
        "KFC" to "Subway",
        "Domino’s" to "Pizza Hut",
        "Nespresso" to "Senseo",
        "Nintendo Switch" to "Steam Deck",
        "TikTok" to "Instagram",
        "Booking.com" to "Airbnb",
        "Aldi" to "Lidl",
        "REWE" to "EDEKA",
        "dm" to "Rossmann",
        "Zalando" to "ABOUT YOU",
        "H&M" to "Zara",
        "BMW" to "Mercedes-Benz",
        "LEGO" to "Playmobil",
        "Converse" to "Vans",
        "Dyson" to "Miele"
    )

    private fun pairKey(pair: Pair<String, String>): String =
        listOf(pair.first.trim().lowercase(), pair.second.trim().lowercase()).sorted().joinToString("||")

    private fun applyExtraBrandPairs() {
        val current = HarmonyPacksData.PACKS
        val pack = current.firstOrNull { it.id == "markenalltag" } ?: return
        val seen = pack.pairs.map(::pairKey).toMutableSet()
        val additions = extraBrandPairs.filter { seen.add(pairKey(it)) }
        if (additions.isEmpty()) return
        val updated = pack.copy(pairs = pack.pairs + additions)
        HarmonyPacksData.setDynamicPacks(current.map { if (it.id == "markenalltag") updated else it })
    }

    private fun extractZip(
        input: InputStream,
        outputDir: File,
        expectedFiles: Set<String>
    ) {
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val name = entry.name.substringAfterLast('/')
                    if (name in expectedFiles) {
                        File(outputDir, name).outputStream().buffered().use { out ->
                            zip.copyTo(out)
                        }
                    }
                }
                zip.closeEntry()
            }
        }
    }

    fun install(context: Context): Map<String, String> {
        // Re-apply the locale-specific cuisine packs and stable local image keys.
        // The installer also observes language changes, so the Italian/Polish
        // cuisine deck switches without replacing newer dynamic content.
        CuisinePackInstaller.install(context)
        applyExtraBrandPairs()

        val outputDir = File(context.filesDir, OUTPUT_DIR).apply { mkdirs() }
        val expectedFiles = (driveOptionToFile.values + brandOptionToFile.values).toSet()
        val needsInstall = expectedFiles.any { !File(outputDir, it).isFile }

        if (needsInstall) {
            outputDir.listFiles()?.forEach { it.delete() }

            context.assets.open(DRIVE_ASSET_ZIP).use { input ->
                extractZip(input, outputDir, expectedFiles)
            }

            val encodedBrandZip = buildString {
                BRAND_ASSET_CHUNKS.forEach { chunkName ->
                    append(
                        context.assets.open(chunkName).bufferedReader().use { reader ->
                            reader.readText()
                        }
                    )
                }
            }
            val brandZipBytes = Base64.decode(encodedBrandZip, Base64.DEFAULT)
            ByteArrayInputStream(brandZipBytes).use { input ->
                extractZip(input, outputDir, expectedFiles)
            }
        }

        val result = LinkedHashMap<String, String>()

        // Keep the original Drive mapping first. In particular, the existing drinks
        // Coca-Cola image remains the canonical Coca-Cola image across the app.
        driveOptionToFile.forEach { (option, fileName) ->
            val file = File(outputDir, fileName)
            if (file.isFile && file.length() > 0L) {
                result[option] = file.absolutePath
            }
        }

        brandOptionToFile.forEach { (option, fileName) ->
            val file = File(outputDir, fileName)
            if (file.isFile && file.length() > 0L && option !in result) {
                result[option] = file.absolutePath
            }
        }

        return result
    }
}
