package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.db.HarmonyDatabase
import com.example.data.model.AnswerEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CoupleStatsEntity
import com.example.data.model.MomentEntity
import com.example.data.model.ProfileEntity
import com.example.ui.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class HarmonyRepository(private val db: HarmonyDatabase) {

    val profileFlow: Flow<ProfileEntity?> = db.profileDao().getProfile()
    val answersFlow: Flow<List<AnswerEntity>> = db.answerDao().getAllAnswers()
    val chatMessagesFlow: Flow<List<ChatMessageEntity>> = db.chatDao().getAllMessages()
    val momentsFlow: Flow<List<MomentEntity>> = db.momentDao().getAllMoments()
    val statsFlow: Flow<CoupleStatsEntity?> = db.coupleStatsDao().getStats()

    suspend fun ensureInitialData() {
        // Initialize profile if not present
        val existingProfile = db.profileDao().getProfile().firstOrNull()
        if (existingProfile == null) {
            db.profileDao().insertOrUpdateProfile(
                ProfileEntity(
                    id = 1,
                    userName = "",
                    partnerName = "",
                    startDate = 0L,
                    simulatorEnabled = true
                )
            )
        }

        // Initialize stats if empty
        val stats = db.coupleStatsDao().getStats().firstOrNull()
        if (stats == null) {
            db.coupleStatsDao().insertOrUpdateStats(CoupleStatsEntity(id = 1, visitedCities = 0, visitedCountries = 0))
        }
    }

    suspend fun updateProfile(userName: String, partnerName: String, startDate: Long) {
        val current = db.profileDao().getProfile().firstOrNull() ?: ProfileEntity()
        db.profileDao().insertOrUpdateProfile(
            current.copy(
                userName = userName,
                partnerName = partnerName,
                startDate = startDate
            )
        )
    }

    suspend fun setSimulatorEnabled(enabled: Boolean) {
        val current = db.profileDao().getProfile().firstOrNull() ?: ProfileEntity()
        db.profileDao().insertOrUpdateProfile(current.copy(simulatorEnabled = enabled))
    }

    suspend fun saveAnswer(packId: String, questionIndex: Int, answerText: String) {
        db.answerDao().insertAnswer(
            AnswerEntity(
                packId = packId,
                questionIndex = questionIndex,
                answerText = answerText
            )
        )
    }

    suspend fun sendChatMessage(text: String, sender: String = "me") {
        db.chatDao().insertMessage(ChatMessageEntity(sender = sender, text = text))
    }

    suspend fun addMoment(title: String, content: String, emoji: String = "💕") {
        db.momentDao().insertMoment(MomentEntity(title = title, content = content, emoji = emoji))
    }

    suspend fun updateStats(cities: Int, countries: Int) {
        db.coupleStatsDao().insertOrUpdateStats(CoupleStatsEntity(id = 1, visitedCities = cities, visitedCountries = countries))
    }

    // --- GEMINI AI FEATURES ---

    suspend fun rephraseGfk(draftText: String, language: AppLanguage): Result<String> {
        val prompt = if (language != AppLanguage.GERMAN) """
            Rephrase the following draft message to my partner using Nonviolent Communication (Rosenberg): observation without judgment, feeling, need, and request. Keep it warm, natural, and suitable for everyday use. Reply in ${language.englishName} and output ONLY the rewritten message.

            Draft: "$draftText"
        """.trimIndent() else """
            Formuliere den folgenden Entwurf für eine Nachricht an meinen Partner nach der Gewaltfreien Kommunikation (Rosenberg) um: Beobachtung ohne Bewertung, Gefühl, Bedürfnis, Bitte. Warmherzig, natürlich, alltagstauglich, auf Deutsch. Gib NUR den umformulierten Text aus.

            Entwurf: "$draftText"
        """.trimIndent()

        return GeminiClient.generateText(prompt)
    }

    suspend fun generateRelationshipCoachAnalysis(
        userName: String,
        partnerName: String,
        recentChats: List<ChatMessageEntity>,
        answers: List<AnswerEntity>,
        language: AppLanguage
    ): Result<String> {
        val chatSummary = recentChats.takeLast(15).joinToString("\n") { m ->
            val senderName = if (m.sender == "me") userName else partnerName
            "$senderName: ${m.text}"
        }

        val answerSummary = answers.takeLast(20).joinToString("\n") { a ->
            if (language != AppLanguage.GERMAN) {
                "Pack '${a.packId}' (question #${a.questionIndex + 1}): ${a.answerText}"
            } else {
                "Paket '${a.packId}' (Frage #${a.questionIndex + 1}): ${a.answerText}"
            }
        }

        val prompt = if (language != AppLanguage.GERMAN) """
            Relationship data for $userName and $partnerName (long-distance relationship):

            Chats:
            ${chatSummary.ifBlank { "(none yet)" }}

            Answered questions:
            ${answerSummary.ifBlank { "(none yet)" }}

            As an empathetic relationship coach drawing on Gottman research, create a concise analysis in ${language.englishName}, organized into:
            1. 📈 Communication patterns
            2. 💪 Your strengths
            3. 🌱 One practical tip for feeling closer despite the distance
        """.trimIndent() else """
            Beziehungsdaten von $userName und $partnerName (Fernbeziehung):

            Chats:
            ${chatSummary.ifBlank { "(noch keine)" }}

            Beantwortete Fragen:
            ${answerSummary.ifBlank { "(noch keine)" }}

            Erstelle als einfühlsamer Beziehungscoach auf Basis der Gottman-Forschung eine kurze Analyse auf Deutsch, gegliedert in:
            1. 📈 Kommunikationsmuster
            2. 💪 Eure Stärken
            3. 🌱 Ein konkreter Tipp für mehr Nähe trotz Distanz
        """.trimIndent()

        return GeminiClient.generateText(prompt)
    }

    suspend fun generateDateIdeas(userName: String, partnerName: String, wishes: String, language: AppLanguage): Result<String> {
        val prompt = if (language != AppLanguage.GERMAN) """
            Generate three creative, specific date ideas for a long-distance couple ($userName and $partnerName).
            Preferences: ${wishes.ifBlank { "none specified" }}.
            The ideas should work either over video call or during their next visit, create emotional closeness, and include specific preparation steps. Reply in ${language.englishName} without an introduction.
        """.trimIndent() else """
            Generiere drei kreative, konkrete Date-Ideen für ein Paar in einer Fernbeziehung ($userName und $partnerName).
            Wünsche: ${wishes.ifBlank { "keine besonderen" }}.
            Die Ideen sollen per Videocall ODER beim nächsten Treffen funktionieren, Nähe schaffen und konkrete Vorbereitungsschritte enthalten. Auf Deutsch, ohne Einleitung.
        """.trimIndent()

        return GeminiClient.generateText(prompt)
    }
}
