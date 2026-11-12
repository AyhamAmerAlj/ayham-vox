package com.ayham.vox.tv

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ayham.vox.core.NetUtil
import com.ayham.vox.core.Protocol
import com.ayham.vox.core.Subtitle
import com.ayham.vox.core.UpdateController
import com.ayham.vox.core.UpdateState
import com.ayham.vox.core.VoxServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TvUiState(
    val ip: String? = null,
    val code: String = "",
    val clients: Int = 0,
    val subtitle: Subtitle? = null,
    val fontSp: Int = 40,
    val atBottom: Boolean = true,
    val error: String? = null,
    val update: UpdateState = UpdateState.Idle,
    val build: Int = 0,
)

class TvViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("vox_tv", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        TvUiState(
            ip = NetUtil.localIpv4(),
            code = (100000..999999).random().toString(),
            fontSp = prefs.getInt("fontSp", 40),
            atBottom = prefs.getBoolean("atBottom", true),
        )
    )
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    private var server: VoxServer? = null
    @Volatile private var clearJob: Job? = null

    private val updater = UpdateController(app, "ayham-vox-tv.apk") { u ->
        _state.update { it.copy(update = u) }
    }

    init {
        _state.update { it.copy(build = updater.installedBuild()) }
        startServer()
        updater.check()
    }

    /** زر ▶ بالريموت: فحص التحديث، وإذا يوجد تحديث يحمّله ويثبّته. */
    fun updateAction() {
        val u = _state.value.update
        if (u is UpdateState.Available || u is UpdateState.NeedPermission) {
            updater.downloadAndInstall()
        } else if (u !is UpdateState.Downloading && u !is UpdateState.Installing) {
            updater.check()
        }
    }

    private fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val s = VoxServer(
                    port = Protocol.PORT,
                    pairingCode = _state.value.code,
                    onSubtitle = { showSubtitle(it) },
                    onClients = { n -> _state.update { it.copy(clients = n) } },
                )
                s.start()
                server = s
            } catch (e: Exception) {
                _state.update { it.copy(error = "تعذّر تشغيل الخادم: ${e.message}") }
            }
        }
    }

    private fun showSubtitle(sub: Subtitle) {
        _state.update { it.copy(subtitle = sub) }
        clearJob?.cancel()
        clearJob = viewModelScope.launch {
            delay(7000)
            _state.update { it.copy(subtitle = null) }
        }
    }

    fun refreshIp() = _state.update { it.copy(ip = NetUtil.localIpv4()) }

    fun biggerFont() = setFont(_state.value.fontSp + 4)

    fun smallerFont() = setFont(_state.value.fontSp - 4)

    private fun setFont(value: Int) {
        val v = value.coerceIn(24, 96)
        prefs.edit().putInt("fontSp", v).apply()
        _state.update { it.copy(fontSp = v) }
    }

    fun togglePosition() {
        val v = !_state.value.atBottom
        prefs.edit().putBoolean("atBottom", v).apply()
        _state.update { it.copy(atBottom = v) }
    }

    override fun onCleared() {
        try {
            server?.stop(500)
        } catch (e: Exception) {
        }
    }
}
