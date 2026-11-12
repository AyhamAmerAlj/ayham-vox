package com.ayham.vox.phone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ayham.vox.core.DialectMode
import com.ayham.vox.core.MockTranslationEngine
import com.ayham.vox.core.Subtitle
import com.ayham.vox.core.SubtitleDeduper
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
    val mode: DialectMode = DialectMode.FUSHA,
    val running: Boolean = false,
    val history: List<Subtitle> = emptyList(),
)

class PhoneViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PhoneUiState())
    val state: StateFlow<PhoneUiState> = _state.asStateFlow()

    private val engine = MockTranslationEngine()
    private val deduper = SubtitleDeduper()
    private val client = VoxClient { s -> _state.update { it.copy(connection = s) } }
    private var job: Job? = null
    private var nextId = 1L

    fun connect(host: String, code: String) = client.connect(host, code)

    fun disconnect() = client.disconnect()

    fun setMode(mode: DialectMode) = _state.update { it.copy(mode = mode) }

    fun toggleMock() {
        if (_state.value.running) {
            job?.cancel()
            _state.update { it.copy(running = false) }
        } else {
            _state.update { it.copy(running = true) }
            job = viewModelScope.launch {
                var i = 0
                while (isActive) {
                    emit(engine.sampleLines[i % engine.sampleLines.size])
                    i++
                    delay(3500)
                }
            }
        }
    }

    fun sendCustom(text: String) {
        if (text.isNotBlank()) emit(text)
    }

    private fun emit(line: String) {
        if (deduper.isDuplicate(line)) return
        val mode = _state.value.mode
        val sub = Subtitle(nextId++, line, engine.translate(line, mode), mode, System.currentTimeMillis())
        client.send(sub)
        _state.update { it.copy(history = (listOf(sub) + it.history).take(100)) }
    }

    override fun onCleared() {
        job?.cancel()
        client.disconnect()
    }
}
