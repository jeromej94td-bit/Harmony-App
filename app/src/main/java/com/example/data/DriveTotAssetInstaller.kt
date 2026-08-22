package com.example.data

import android.content.Context
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Installs the Drive-backed "Das oder das?" image bundle that ships inside the APK.
 * The bundle lives in GitHub at app/src/main/assets/drive_tot_assets.zip.
 *
 * Images are extracted to app-private storage once and returned as option -> local-path
 * mappings. DeveloperDataManager then registers them as generated images, so these local
 * files win over TotImageProvider's network fallback URLs and work offline like the other
 * shipped visual packs.
 */
object DriveTotAssetInstaller {
    private const val ASSET_ZIP = "drive_tot_assets.zip"
    private const val OUTPUT_DIR = "drive_tot_assets_v1"

    private val optionToFile = linkedMapOf(
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

    fun install(context: Context): Map<String, String> {
        val outputDir = File(context.filesDir, OUTPUT_DIR).apply { mkdirs() }
        val expectedFiles = optionToFile.values.toSet()
        val needsInstall = expectedFiles.any { !File(outputDir, it).isFile }

        if (needsInstall) {
            outputDir.listFiles()?.forEach { it.delete() }
            context.assets.open(ASSET_ZIP).use { input ->
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
        }

        return optionToFile.mapNotNull { (option, fileName) ->
            val file = File(outputDir, fileName)
            if (file.isFile && file.length() > 0L) option to file.absolutePath else null
        }.toMap(LinkedHashMap())
    }
}
