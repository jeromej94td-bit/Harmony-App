package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeveloperDataManager
import com.example.data.db.HarmonyDatabase
import com.example.data.model.AnswerEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CoupleStatsEntity
import com.example.data.model.HarmonyPacksData
import com.example.data.model.MomentEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.QuestionPack
import com.example.data.repository.HarmonyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Random

data class ActivePackRun(
    val pack: QuestionPack,
    val currentIndex: Int = 0,
    val currentAnswers: Map<Int, String> = emptyMap(),
    val isFinished: Boolean = false
)

data class HarmonyUiState(
    val selectedTab: Int = 0,
    val profile: ProfileEntity = ProfileEntity(),
    val answers: List<AnswerEntity> = emptyList(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val moments: List<MomentEntity> = emptyList(),
    val stats: CoupleStatsEntity = CoupleStatsEntity(),
    val packFilter: String = "all", // "all", "open", "done"
    val selectedTopicId: String? = null,
    val selectedCategoryId: String? = null,
    val activeRun: ActivePackRun? = null,
    val isExitConfirmOpen: Boolean = false,
    val isOwnAnswerDialogOpen: Boolean = false,
    val ownAnswerTargetIndex: Int? = null,
    val ownAnswerMode: String? = null, // null or "disc"
    val isProfileSheetOpen: Boolean = false,
    val isEditProfileOpen: Boolean = false,
    val isAddMomentOpen: Boolean = false,
    val gfkPanelOpen: Boolean = false,
    val gfkLoading: Boolean = false,
    val gfkResult: String? = null,
    val coachLoading: Boolean = false,
    val coachResult: String? = null,
    val dateIdeasLoading: Boolean = false,
    val dateIdeasResult: String? = null,
    val toastMessage: String? = null
)

class HarmonyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HarmonyDatabase.getInstance(application)
    private val repository = HarmonyRepository(db)

    private val _selectedTab = MutableStateFlow(0)
    private val _packFilter = MutableStateFlow("all")
    private val _selectedTopicId = MutableStateFlow<String?>(null)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _activeRun = MutableStateFlow<ActivePackRun?>(null)

    private val _isExitConfirmOpen = MutableStateFlow(false)
    private val _isOwnAnswerDialogOpen = MutableStateFlow(false)
    private val _ownAnswerTargetIndex = MutableStateFlow<Int?>(null)
    private val _ownAnswerMode = MutableStateFlow<String?>(null)

    private val _isProfileSheetOpen = MutableStateFlow(false)
    private val _isEditProfileOpen = MutableStateFlow(false)
    private val _isAddMomentOpen = MutableStateFlow(false)

    private val _gfkPanelOpen = MutableStateFlow(false)
    private val _gfkLoading = MutableStateFlow(false)
    private val _gfkResult = MutableStateFlow<String?>(null)

    private val _coachLoading = MutableStateFlow(false)
    private val _coachResult = MutableStateFlow<String?>(null)

    private val _dateIdeasLoading = MutableStateFlow(false)
    private val _dateIdeasResult = MutableStateFlow<String?>(null)

    private val _toastMessage = MutableStateFlow<String?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<HarmonyUiState> = combine(
        _selectedTab,
        repository.profileFlow,
        repository.answersFlow,
        repository.chatMessagesFlow,
        repository.momentsFlow,
        repository.statsFlow,
        _packFilter,
        _selectedTopicId,
        _selectedCategoryId,
        _activeRun,
        _isExitConfirmOpen,
        _isOwnAnswerDialogOpen,
        _isProfileSheetOpen,
        _isEditProfileOpen,
        _isAddMomentOpen,
        _gfkPanelOpen,
        _gfkLoading,
        _gfkResult,
        _coachLoading,
        _coachResult,
        _dateIdeasLoading,
        _dateIdeasResult,
        _toastMessage
    ) { arrayOfValues ->
        HarmonyUiState(
            selectedTab = arrayOfValues[0] as Int,
            profile = (arrayOfValues[1] as? ProfileEntity) ?: ProfileEntity(),
            answers = (arrayOfValues[2] as? List<AnswerEntity>) ?: emptyList(),
            messages = (arrayOfValues[3] as? List<ChatMessageEntity>) ?: emptyList(),
            moments = (arrayOfValues[4] as? List<MomentEntity>) ?: emptyList(),
            stats = (arrayOfValues[5] as? CoupleStatsEntity) ?: CoupleStatsEntity(),
            packFilter = arrayOfValues[6] as String,
            selectedTopicId = arrayOfValues[7] as? String,
            selectedCategoryId = arrayOfValues[8] as? String,
            activeRun = arrayOfValues[9] as? ActivePackRun,
            isExitConfirmOpen = arrayOfValues[10] as Boolean,
            isOwnAnswerDialogOpen = arrayOfValues[11] as Boolean,
            ownAnswerTargetIndex = _ownAnswerTargetIndex.value,
            ownAnswerMode = _ownAnswerMode.value,
            isProfileSheetOpen = arrayOfValues[12] as Boolean,
            isEditProfileOpen = arrayOfValues[13] as Boolean,
            isAddMomentOpen = arrayOfValues[14] as Boolean,
            gfkPanelOpen = arrayOfValues[15] as Boolean,
            gfkLoading = arrayOfValues[16] as Boolean,
            gfkResult = arrayOfValues[17] as? String,
            coachLoading = arrayOfValues[18] as Boolean,
            coachResult = arrayOfValues[19] as? String,
            dateIdeasLoading = arrayOfValues[20] as Boolean,
            dateIdeasResult = arrayOfValues[21] as? String,
            toastMessage = arrayOfValues[22] as? String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HarmonyUiState()
    )

    init {
        DeveloperDataManager.init(application)
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setPackFilter(filter: String) {
        _packFilter.value = filter
    }

    fun openTopic(topicId: String) {
        _selectedTopicId.value = topicId
        _selectedCategoryId.value = null
        _selectedTab.value = 2 // Pack list tab
    }

    fun openCategory(catId: String) {
        _selectedCategoryId.value = catId
        _selectedTopicId.value = null
        _selectedTab.value = 2 // Pack list tab
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(2400)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun sendWidget(name: String, emoji: String) {
        showToast("$emoji „$name\" an Alex gesendet")
    }

    // --- QUIZ RUNNER ---

    fun startPack(packId: String) {
        val pack = HarmonyPacksData.PACKS.find { it.id == packId } ?: return
        val currentAnswers = uiState.value.answers.filter { it.packId == packId }
            .associate { it.questionIndex to it.answerText }
        _activeRun.value = ActivePackRun(
            pack = pack,
            currentIndex = 0,
            currentAnswers = currentAnswers,
            isFinished = false
        )
    }

    fun pickAnswer(optionText: String) {
        val run = _activeRun.value ?: return
        val updatedAnswers = run.currentAnswers.toMutableMap()
        updatedAnswers[run.currentIndex] = optionText
        _activeRun.value = run.copy(currentAnswers = updatedAnswers)
        nextStep()
    }

    fun nextStep() {
        val run = _activeRun.value ?: return
        val totalLen = if (run.pack.type == "tot") run.pack.pairs.size else run.pack.questions.size
        val nextIndex = run.currentIndex + 1
        if (nextIndex >= totalLen) {
            finishPack()
        } else {
            _activeRun.value = run.copy(currentIndex = nextIndex)
        }
    }

    fun finishPack() {
        val run = _activeRun.value ?: return
        viewModelScope.launch {
            run.currentAnswers.forEach { (index, answerText) ->
                repository.saveAnswer(run.pack.id, index, answerText)
            }
            _activeRun.value = run.copy(isFinished = true)
        }
    }

    fun askExitRun() {
        val run = _activeRun.value
        if (run != null && run.pack.type == "disc") {
            closeRunner()
        } else {
            _isExitConfirmOpen.value = true
        }
    }

    fun closeExitConfirm() {
        _isExitConfirmOpen.value = false
    }

    fun closeRunner() {
        val run = _activeRun.value
        if (run != null) {
            viewModelScope.launch {
                run.currentAnswers.forEach { (index, answerText) ->
                    repository.saveAnswer(run.pack.id, index, answerText)
                }
                _activeRun.value = null
                _isExitConfirmOpen.value = false
            }
        } else {
            _activeRun.value = null
            _isExitConfirmOpen.value = false
        }
    }

    fun openOwnAnswerDialog(index: Int? = null, mode: String? = null) {
        _ownAnswerTargetIndex.value = index
        _ownAnswerMode.value = mode
        _isOwnAnswerDialogOpen.value = true
    }

    fun closeOwnAnswerDialog() {
        _isOwnAnswerDialogOpen.value = false
        _ownAnswerTargetIndex.value = null
        _ownAnswerMode.value = null
    }

    fun saveOwnAnswer(answerText: String) {
        val run = _activeRun.value ?: return
        val idx = _ownAnswerTargetIndex.value ?: run.currentIndex
        val updatedAnswers = run.currentAnswers.toMutableMap()
        updatedAnswers[idx] = answerText
        _activeRun.value = run.copy(currentAnswers = updatedAnswers)
        closeOwnAnswerDialog()
        nextStep()
    }

    // --- CHAT ---

    private val SIM_REPLIES = listOf(
        "Das klingt schön 🥰",
        "Ich vermiss dich gerade richtig",
        "Erzähl mir mehr davon 💕",
        "Haha du bringst mich immer zum Lachen 😄",
        "Können wir heute Abend telefonieren?",
        "Ich denk so oft an dich ❤️"
    )

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(text, sender = "me")
            val profile = uiState.value.profile
            if (profile.simulatorEnabled) {
                delay(1200)
                val reply = SIM_REPLIES[Random().nextInt(SIM_REPLIES.size)]
                repository.sendChatMessage(reply, sender = "them")
            }
        }
    }

    fun toggleGfkPanel() {
        _gfkPanelOpen.value = !_gfkPanelOpen.value
    }

    fun runGfk(draftText: String) {
        if (draftText.isBlank()) return
        _gfkLoading.value = true
        _gfkResult.value = null
        viewModelScope.launch {
            val result = repository.rephraseGfk(draftText)
            _gfkLoading.value = false
            _gfkResult.value = result.getOrElse { "Fehler bei der Umformulierung: ${it.localizedMessage}" }
        }
    }

    // --- MOMENTS ---

    fun openAddMomentDialog() {
        _isAddMomentOpen.value = true
    }

    fun closeAddMomentDialog() {
        _isAddMomentOpen.value = false
    }

    fun addMoment(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.addMoment(title, content)
            _isAddMomentOpen.value = false
            showToast("Moment gespeichert 💞")
        }
    }

    // --- PROFILE & AI COACH / DATE IDEAS ---

    fun openProfileSheet() {
        _isProfileSheetOpen.value = true
    }

    fun closeProfileSheet() {
        _isProfileSheetOpen.value = false
    }

    fun toggleSimulator() {
        val profile = uiState.value.profile
        viewModelScope.launch {
            repository.setSimulatorEnabled(!profile.simulatorEnabled)
        }
    }

    fun openEditProfile() {
        _isEditProfileOpen.value = true
    }

    fun closeEditProfile() {
        _isEditProfileOpen.value = false
    }

    fun saveEditProfile(userName: String, partnerName: String, startDate: Long) {
        viewModelScope.launch {
            repository.updateProfile(userName, partnerName, startDate)
            _isEditProfileOpen.value = false
            showToast("Profil gespeichert")
        }
    }

    fun runCoach() {
        _coachLoading.value = true
        _coachResult.value = null
        val profile = uiState.value.profile
        val chats = uiState.value.messages
        val answers = uiState.value.answers
        viewModelScope.launch {
            val result = repository.generateRelationshipCoachAnalysis(
                userName = profile.userName,
                partnerName = profile.partnerName,
                recentChats = chats,
                answers = answers
            )
            _coachLoading.value = false
            _coachResult.value = result.getOrElse { "Fehler bei der Analyse: ${it.localizedMessage}" }
        }
    }

    fun runDateIdeas(wishes: String) {
        _dateIdeasLoading.value = true
        _dateIdeasResult.value = null
        val profile = uiState.value.profile
        viewModelScope.launch {
            val result = repository.generateDateIdeas(
                userName = profile.userName,
                partnerName = profile.partnerName,
                wishes = wishes
            )
            _dateIdeasLoading.value = false
            _dateIdeasResult.value = result.getOrElse { "Fehler bei der Ideengenerierung: ${it.localizedMessage}" }
        }
    }
}
