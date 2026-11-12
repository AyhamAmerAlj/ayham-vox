package com.ayham.vox.core

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** يعمل على الهاتف: يتصل بالتلفزيون ويعيد المحاولة تلقائياً عند الانقطاع. */
class VoxClient(private val onState: (State) -> Unit) {

    enum class State { DISCONNECTED, CONNECTING, CONNECTED, DENIED }

    @Volatile private var socket: WebSocketClient? = null
    @Volatile private var wanted = false
    private var host = ""
    private var pairingCode = ""
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun connect(host: String, pairingCode: String) {
        this.host = host.trim()
        this.pairingCode = pairingCode.trim()
        wanted = true
        socket?.close()
        openSocket()
    }

    fun disconnect() {
        wanted = false
        socket?.close()
        onState(State.DISCONNECTED)
    }

    fun send(subtitle: Subtitle): Boolean {
        val s = socket
        return if (s != null && s.isOpen) {
            s.send(Protocol.subtitle(subtitle))
            true
        } else {
            false
        }
    }

    private fun openSocket() {
        if (!wanted) return
        onState(State.CONNECTING)
        val uri = try {
            URI("ws://$host:${Protocol.PORT}")
        } catch (e: Exception) {
            wanted = false
            onState(State.DISCONNECTED)
            return
        }
        val client = object : WebSocketClient(uri) {
            override fun onOpen(handshake: ServerHandshake?) {
                send(Protocol.hello(pairingCode))
            }

            override fun onMessage(message: String?) {
                when (Protocol.parse(message ?: "")) {
                    Protocol.Message.Ok -> onState(State.CONNECTED)
                    Protocol.Message.Deny -> {
                        wanted = false
                        onState(State.DENIED)
                    }
                    else -> {}
                }
            }

            override fun onClose(status: Int, reason: String?, remote: Boolean) {
                if (wanted) {
                    onState(State.DISCONNECTED)
                    scheduleRetry()
                }
            }

            override fun onError(ex: Exception?) {}
        }
        socket = client
        client.connect()
    }

    private fun scheduleRetry() {
        scheduler.schedule({ openSocket() }, 2, TimeUnit.SECONDS)
    }
}
