package com.example.data

import android.content.Context
import android.util.Base64
import com.example.data.model.HarmonyPacksData
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/**
 * Installs the bundled "Das oder das?" image assets that ship inside the APK.
 * Existing Drive-backed images stay in drive_tot_assets.zip. Generated bundles
 * are stored as split Base64 text assets and extracted to app-private storage.
 */
object DriveTotAssetInstaller {
    private const val DRIVE_ASSET_ZIP = "drive_tot_assets.zip"
    private const val OUTPUT_DIR = "drive_tot_assets_v3"
    private const val INSTALL_MARKER = ".install-complete"

    private val BRAND_ASSET_CHUNKS = listOf(
        "brand_everyday_assets_01.b64",
        "brand_everyday_assets_02.b64",
        "brand_everyday_assets_03_04.b64",
        "brand_everyday_assets_05_06.b64",
        "brand_everyday_assets_07_08.b64"
    )

    private val ENGAGEMENT_RING_CHUNKS = listOf(
        "engagement_rings_01.b64",
        "engagement_rings_02.b64",
        "engagement_rings_03.b64",
        "engagement_rings_04.b64",
        "engagement_rings_05.b64",
        "engagement_rings_06.b64",
        "engagement_rings_07.b64"
    )

    private val driveOptionToFile = linkedMapOf(
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

    private val ringPairs = listOf(
        "Klassisch Solitär" to "Vintage verspielt",
        "Schmal & zart" to "Markant & breit",
        "Ovaler Diamant" to "Runder Diamant",
        "Großer Stein" to "Diamanten im Band",
        "Vintage Art déco" to "Modern geometrisch",
        "Moissanit" to "Saphir",
        "Diamant" to "Farbedelstein",
        "Ohne Stein" to "Statement-Ring",
        "Platin" to "Roségold",
        "Drei-Stein-Ring" to "Moderner Solitär",
        "Gelbgold" to "Weißgold"
    )

    private val ringOptionToFile = linkedMapOf(
        "Klassisch Solitär" to "ring_drive_01_a.webp",
        "Vintage verspielt" to "ring_drive_01_b.webp",
        "Schmal & zart" to "ring_drive_02_a.webp",
        "Markant & breit" to "ring_drive_02_b.webp",
        "Ovaler Diamant" to "ring_drive_03_a.webp",
        "Runder Diamant" to "ring_drive_03_b.webp",
        "Großer Stein" to "ring_drive_04_a.webp",
        "Diamanten im Band" to "ring_drive_04_b.webp",
        "Vintage Art déco" to "ring_drive_05_a.webp",
        "Modern geometrisch" to "ring_drive_05_b.webp",
        "Moissanit" to "ring_drive_06_a.webp",
        "Saphir" to "ring_drive_06_b.webp",
        "Diamant" to "ring_drive_07_a.webp",
        "Farbedelstein" to "ring_drive_07_b.webp",
        "Ohne Stein" to "ring_drive_08_a.webp",
        "Statement-Ring" to "ring_drive_08_b.webp",
        "Platin" to "ring_drive_09_a.webp",
        "Roségold" to "ring_drive_09_b.webp",
        "Drei-Stein-Ring" to "ring_drive_10_a.webp",
        "Moderner Solitär" to "ring_drive_10_b.webp",
        "Gelbgold" to "ring_drive_11_a.webp",
        "Weißgold" to "ring_drive_11_b.webp"
    )

    private fun applyEngagementRingPack() {
        val current = HarmonyPacksData.PACKS
        val ringPack = current.firstOrNull { it.id == "ringe" } ?: return
        val updated = ringPack.copy(pairs = ringPairs)
        HarmonyPacksData.setDynamicPacks(current.map { if (it.id == "ringe") updated else it })
    }

    private fun extractZip(input: InputStream, outputDir: File, expectedFiles: Set<String>): Boolean {
        val extractedFiles = mutableListOf<File>()
        return try {
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (!entry.isDirectory) {
                        val name = entry.name.substringAfterLast('/')
                        if (name in expectedFiles) {
                            val outputFile = File(outputDir, name)
                            extractedFiles += outputFile
                            outputFile.outputStream().buffered().use { out -> zip.copyTo(out) }
                        }
                    }
                    zip.closeEntry()
                }
            }
            true
        } catch (_: ZipException) {
            extractedFiles.forEach { it.delete() }
            false
        }
    }

    private fun decodeChunkedZip(context: Context, chunks: List<String>): ByteArray {
        val encoded = buildString {
            chunks.forEach { chunkName ->
                append(context.assets.open(chunkName).bufferedReader().use { it.readText() })
            }
        }
        return Base64.decode(encoded, Base64.DEFAULT)
    }

    fun install(context: Context): Map<String, String> {
        CuisinePackInstaller.install(context)
        applyEngagementRingPack()

        val outputDir = File(context.filesDir, OUTPUT_DIR).apply { mkdirs() }
        val installMarker = File(outputDir, INSTALL_MARKER)
        val expectedFiles = (driveOptionToFile.values + brandOptionToFile.values + ringOptionToFile.values).toSet()
        val needsInstall = !installMarker.isFile

        if (needsInstall) {
            outputDir.listFiles()?.forEach { it.delete() }
            context.assets.open(DRIVE_ASSET_ZIP).use { extractZip(it, outputDir, expectedFiles) }
            ByteArrayInputStream(decodeChunkedZip(context, BRAND_ASSET_CHUNKS)).use {
                extractZip(it, outputDir, expectedFiles)
            }
            ByteArrayInputStream(decodeChunkedZip(context, ENGAGEMENT_RING_CHUNKS)).use {
                extractZip(it, outputDir, expectedFiles)
            }
            installMarker.writeText("1")
        }

        val result = LinkedHashMap<String, String>()
        driveOptionToFile.forEach { (option, fileName) ->
            val file = File(outputDir, fileName)
            if (file.isFile && file.length() > 0L) result[option] = file.absolutePath
        }
        brandOptionToFile.forEach { (option, fileName) ->
            val file = File(outputDir, fileName)
            if (file.isFile && file.length() > 0L && option !in result) result[option] = file.absolutePath
        }
        ringOptionToFile.forEach { (option, fileName) ->
            val file = File(outputDir, fileName)
            if (file.isFile && file.length() > 0L) result[option] = file.absolutePath
        }
        return result
    }
}
