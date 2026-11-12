package com.ayham.vox.phone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ayham.vox.core.MockTranslationEngine
import com.ayham.vox.core.Subtitle
import com.ayham.vox.core.SubtitleDeduper
import com.ayham.vox.core.UpdateController
import com.ayham.vox.core.UpdateState
import com.ayham.vox.core.VoxClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PhoneUiState(
    val connection: VoxClient.State = VoxClient.State.DISCONNECTED,
    val mockRunning: Boolean = false,
    val listening: Boolean = false,
    val partial: String = "",
    val modelReady: Boolean = false,
    val engineStatus: String = "",
    val problem: String? = null,
    val history: List<Subtitle> = emptyList(),
    val update: UpdateState = UpdateState.Idle,
    val build: Int = 0,
)

class PhoneViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PhoneUiState())
    val state: StateFlow<PhoneUiState> = _state.asStateFlow()

    private val mock = MockTranslationEngine()
    private val translator = MlKitTranslator()
    private val deduper = SubtitleDeduper()
    private val client = VoxClient { s -> _state.update { it.copy(connection = s) } }
    private var mockJob: Job? = null
    private var nextId = 1L

    private val speech = SpeechInput(
        context = app,
        onPartial = { t -> _state.update { it.copy(partial = t) } },
        onFinal = { t -> onSpeechFinal(t) },
        onProblem = { m -> _state.update { it.copy(problem = m) } },
    )

    private val updater = UpdateController(app, "ayham-vox-phone.apk") { u ->
        _state.update { it.copy(update = u) }
    }

    init {
        _state.update { it.copy(build = updater.installedBuild()) }
        updater.check()
        prepareTranslator()
    }

    private fun prepareTranslator() {
        _state.update { it.copy(modelReady = false, engineStatus = "جاري تجهيز نموذج الترجمة (مرة واحدة، يحتاج إنترنت)...") }
        viewModelScope.launch {
            try {
                translator.prepare()
                _state.update { it.copy(modelReady = true, engineStatus = "نموذج الترجمة جاهز ✓") }
            } catch (e: Exception) {
                _state.update { it.copy(engineStatus = "تعذّر تحميل نموذج الترجمة. تأكد من الإنترنت ثم اضغط إعادة المحاولة.") }
            }
        }
    }

    fun retryModel() = prepareTranslator()

    fun checkUpdate() = updater.check()

    fun installUpdate() = updater.downloadAndInstall()

    fun connect(host: String, code: String) = client.connect(host, code)

    fun disconnect() = client.disconnect()

    // ---------- الاستماع الحي ----------

    fun startListening() {
        if (!_state.value.modelReady) {
            _state.update { it.copy(problem = "نموذج الترجمة غير جاهز بعد") }
            return
        }
        _state.update { it.copy(listening = true, problem = null, partial = "") }
        speech.start()
    }

    fun stopListening() {
        speech.stop()
        _state.update { it.copy(listening = false, partial = "") }
    }

    fun onMicDenied() = _state.update { it.copy(problem = "لازم تسمح بالميكروفون حتى يشتغل الاستماع") }

    private fun onSpeechFinal(text: String) {
        _state.update { it.copy(partial = "") }
        if (deduper.isDuplicate(text)) return
        viewModelScope.launch {
            try {
                publish(text, translator.translate(text))
            } catch (e: Exception) {
                _state.update { it.copy(problem = "تعذّرت الترجمة: ${e.message}") }
            }
        }
    }

    // ---------- وضع التجربة ----------

    fun toggleMock() {
        if (_state.value.mockRunning) {
            mockJob?.cancel()
            _state.update { it.copy(mockRunning = false) }
        } else {
            _state.update { it.copy(mockRunning = true) }
            mockJob = viewModelScope.launch {
                var i = 0
                while (isActive) {
                    val line = mock.sampleLines[i % mock.sampleLines.size]
                    i++
                    if (!deduper.isDuplicate(line)) publish(line, mock.translate(line))
                    delay(3500)
                }
            }
        }
    }

    private fun publish(original: String, translated: String) {
        val sub = Subtitle(nextId++, original, translated, System.currentTimeMillis())
        client.send(sub)
        _state.update { it.copy(history = (listOf(sub) + it.history).take(100)) }
    }

    override fun onCleared() {
        mockJob?.cancel()
        speech.stop()
        translator.close()
        client.disconnect()
    }
}
