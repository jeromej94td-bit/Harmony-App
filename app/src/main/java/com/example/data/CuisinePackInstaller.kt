package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.HarmonyPacksData
import com.example.data.model.QuestionPack
import com.example.ui.components.TotImageProvider

/**
 * Compatibility layer for the locale-specific cuisine decks that originally
 * lived on the older localization branches (PRs #16/#17/#21).
 *
 * It deliberately layers the decks onto the current dynamic pack state instead
 * of replacing Models.kt or DeveloperDataManager. That keeps newer generated,
 * custom, Drive and Marken-&-Alltag content intact.
 */
object CuisinePackInstaller {
    private const val SETTINGS_PREFS = "harmony_settings_prefs"
    private const val LANGUAGE_KEY = "app_language"
    private const val ITALIAN_PACK_ID = "tot_italian_cuisine_mixed"
    private const val POLISH_PACK_ID = "tot_polish_cuisine_traditional"

    private val cuisineIds = setOf(ITALIAN_PACK_ID, POLISH_PACK_ID)
    private var observedPrefs: SharedPreferences? = null
    private var languageListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val italianPack = QuestionPack(
        id = ITALIAN_PACK_ID,
        title = "🍝 Cucina italiana — scelte regionali",
        tags = listOf("dasoderdas", "cucina", "italia"),
        cat = "tot",
        topic = "essen",
        type = "tot",
        emoji = "🍝",
        pairs = listOf(
            "Pizza napoletana" to "Calzone",
            "Sformatino di zucchine con fonduta di pecorino" to "Pappa al pomodoro",
            "Spaghetti alle vongole" to "Fritto misto di mare",
            "Carbonara" to "Amatriciana",
            "Pasta alla Norma" to "Risotto ai funghi",
            "Pappardelle al cinghiale" to "Pici senesi al ragù di chianina",
            "Lasagne alla bolognese" to "Cannelloni",
            "Cacciucco" to "Brodetto di pesce",
            "Risotto alla milanese" to "Polenta",
            "Arancini" to "Supplì",
            "Ribollita" to "Panzanella",
            "Bruschetta al pomodoro e basilico" to "Crostini toscani con fegato di pollo",
            "Pesto alla genovese" to "Ragù alla bolognese",
            "Baccalà mantecato" to "Sarde in saor",
            "Orecchiette alle cime di rapa" to "Trofie al pesto",
            "Insalata Caprese" to "Fiori di zucca ripieni",
            "Parmigiana di melanzane" to "Caponata",
            "Trippa alla fiorentina" to "Peposo all’Impruneta",
            "Risotto alla pescatora" to "Orata al cartoccio",
            "Ossobuco" to "Saltimbocca alla romana",
            "Gnocchi alla sorrentina" to "Pasta e fagioli",
            "Bistecca alla fiorentina" to "Arrosticini",
            "Insalata di polpo" to "Spaghetti allo scoglio",
            "Focaccia genovese" to "Piadina romagnola",
            "Carciofi alla romana" to "Polenta ai funghi",
            "Gnocchi" to "Ravioli",
            "Cantucci con vin santo" to "Tortino al cioccolato con cuore caldo",
            "Tiramisù" to "Panna cotta",
            "Cannoli siciliani" to "Sfogliatella",
            "Gelato" to "Semifreddo"
        )
    )

    private val polishPack = QuestionPack(
        id = POLISH_PACK_ID,
        title = "🇵🇱 Tradycyjna kuchnia polska",
        tags = listOf("dasoderdas", "kuchnia", "polska"),
        cat = "tot",
        topic = "essen",
        type = "tot",
        emoji = "🇵🇱",
        pairs = listOf(
            "Pierogi ruskie" to "Bigos",
            "Żurek" to "Barszcz czerwony",
            "Kotlet schabowy" to "Placki ziemniaczane",
            "Gołąbki" to "Kopytka",
            "Rosół" to "Flaki",
            "Żeberka w kapuście" to "Kaszanka",
            "Łazanki" to "Zrazy wołowe",
            "Kaczka po poznańsku" to "Gulasz",
            "Ryba po grecku" to "Śledź w śmietanie",
            "Placki po węgiersku" to "Racuchy",
            "Pyzy ziemniaczane" to "Kartacze",
            "Oscypek z żurawiną" to "Bryndza",
            "Makowiec" to "Sernik",
            "Szarlotka" to "Pączki"
        )
    )

    private val italianImages = listOf(
        R.drawable.it_01_pizza_napoletana to R.drawable.it_01_calzone,
        R.drawable.vespucci_02_sformatino_zucchine_pecorino to R.drawable.vespucci_02_pappa_al_pomodoro,
        R.drawable.it_17_spaghetti_alle_vongole to R.drawable.it_17_fritto_misto_di_mare,
        R.drawable.it_02_carbonara to R.drawable.it_02_amatriciana,
        R.drawable.it_16_pasta_alla_norma to R.drawable.it_16_risotto_ai_funghi,
        R.drawable.vespucci_03_pappardelle_al_cinghiale to R.drawable.vespucci_03_pici_senesi_ragu_chianina,
        R.drawable.it_03_lasagne_alla_bolognese to R.drawable.it_03_cannelloni,
        R.drawable.it_19_cacciucco to R.drawable.it_19_brodetto_di_pesce,
        R.drawable.it_04_risotto_alla_milanese to R.drawable.it_04_polenta,
        R.drawable.it_05_arancini to R.drawable.it_05_suppli,
        R.drawable.it_18_ribollita to R.drawable.it_18_panzanella,
        R.drawable.vespucci_01_bruschetta_pomodoro_basilico to R.drawable.vespucci_01_crostini_toscani_fegato_pollo,
        R.drawable.it_06_pesto_alla_genovese to R.drawable.it_06_ragu_alla_bolognese,
        R.drawable.it_21_baccala_mantecato to R.drawable.it_21_sarde_in_saor,
        R.drawable.it_07_orecchiette_alle_cime_di_rapa to R.drawable.it_07_trofie_al_pesto,
        R.drawable.it_20_insalata_caprese to R.drawable.it_20_fiori_di_zucca_ripieni,
        R.drawable.it_08_parmigiana_di_melanzane to R.drawable.it_08_caponata,
        R.drawable.vespucci_04_trippa_alla_fiorentina to R.drawable.vespucci_04_peposo_all_impruneta,
        R.drawable.it_23_risotto_alla_pescatora to R.drawable.it_23_orata_al_cartoccio,
        R.drawable.it_09_ossobuco to R.drawable.it_09_saltimbocca_alla_romana,
        R.drawable.it_22_gnocchi_alla_sorrentina to R.drawable.it_22_pasta_e_fagioli,
        R.drawable.it_10_bistecca_alla_fiorentina to R.drawable.it_10_arrosticini,
        R.drawable.it_25_insalata_di_polpo to R.drawable.it_25_spaghetti_allo_scoglio,
        R.drawable.it_11_focaccia_genovese to R.drawable.it_11_piadina_romagnola,
        R.drawable.it_24_carciofi_alla_romana to R.drawable.it_24_polenta_ai_funghi,
        R.drawable.it_12_gnocchi to R.drawable.it_12_ravioli,
        R.drawable.vespucci_05_cantucci_vin_santo to R.drawable.vespucci_05_tortino_cioccolato_cuore_caldo,
        R.drawable.it_13_tiramisu to R.drawable.it_13_panna_cotta,
        R.drawable.it_14_cannoli_siciliani to R.drawable.it_14_sfogliatella,
        R.drawable.it_15_gelato to R.drawable.it_15_semifreddo
    )

    private val polishImages = listOf(
        R.drawable.pl_01_pierogi_ruskie to R.drawable.pl_01_bigos,
        R.drawable.pl_02_zurek to R.drawable.pl_02_barszcz_czerwony,
        R.drawable.pl_03_kotlet_schabowy to R.drawable.pl_03_placki_ziemniaczane,
        R.drawable.pl_04_golabki to R.drawable.pl_04_kopytka,
        R.drawable.pl_05_rosol to R.drawable.pl_05_flaki,
        R.drawable.pl_06_zeberka_w_kapuscie to R.drawable.pl_06_kaszanka,
        R.drawable.pl_07_lazanki to R.drawable.pl_07_zrazy_wolowe,
        R.drawable.pl_08_kaczka_po_poznansku to R.drawable.pl_08_gulasz,
        R.drawable.pl_09_ryba_po_grecku to R.drawable.pl_09_sledz_w_smietanie,
        R.drawable.pl_10_placki_po_wegiersku to R.drawable.pl_10_racuchy,
        R.drawable.pl_11_pyzy_ziemniaczane to R.drawable.pl_11_kartacze,
        R.drawable.pl_12_oscypek_z_zurawina to R.drawable.pl_12_bryndza,
        R.drawable.pl_13_makowiec to R.drawable.pl_13_sernik,
        R.drawable.pl_14_szarlotka to R.drawable.pl_14_paczki
    )

    fun install(context: Context) {
        registerImages()
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

        if (observedPrefs !== prefs) {
            observedPrefs?.let { oldPrefs ->
                languageListener?.let { listener ->
                    oldPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { changedPrefs, key ->
                if (key == LANGUAGE_KEY) {
                    applyLanguage(changedPrefs.getString(LANGUAGE_KEY, "de") ?: "de")
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            observedPrefs = prefs
            languageListener = listener
        }

        applyLanguage(prefs.getString(LANGUAGE_KEY, "de") ?: "de")
    }

    private fun registerImages() {
        italianPack.pairs.zip(italianImages).forEachIndexed { index, (pair, images) ->
            TotImageProvider.setGeneratedImage(pair.first, images.first)
            TotImageProvider.setGeneratedImage(pair.second, images.second)
            TotImageProvider.setGeneratedImage("tot:$ITALIAN_PACK_ID:$index:a", images.first)
            TotImageProvider.setGeneratedImage("tot:$ITALIAN_PACK_ID:$index:b", images.second)
        }
        polishPack.pairs.zip(polishImages).forEachIndexed { index, (pair, images) ->
            TotImageProvider.setGeneratedImage(pair.first, images.first)
            TotImageProvider.setGeneratedImage(pair.second, images.second)
            TotImageProvider.setGeneratedImage("tot:$POLISH_PACK_ID:$index:a", images.first)
            TotImageProvider.setGeneratedImage("tot:$POLISH_PACK_ID:$index:b", images.second)
        }
    }

    @Synchronized
    private fun applyLanguage(languageCode: String) {
        val defaultById = HarmonyPacksData.DEFAULT_PACKS.associateBy { it.id }
        val preservedDynamic = HarmonyPacksData.PACKS.filter { pack ->
            pack.id !in cuisineIds && (defaultById[pack.id] == null || defaultById[pack.id] != pack)
        }
        val localePack = when (languageCode.lowercase().substringBefore('-')) {
            "it" -> listOf(italianPack)
            "pl" -> listOf(polishPack)
            else -> emptyList()
        }
        HarmonyPacksData.setDynamicPacks(preservedDynamic + localePack)
    }
}
