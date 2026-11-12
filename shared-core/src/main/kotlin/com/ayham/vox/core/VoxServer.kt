package com.ayham.vox.core

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.Collections

/** يعمل على التلفزيون: يستقبل الترجمات من الهاتف بعد التحقق من رمز الاقتران. */
class VoxServer(
    port: Int,
    private val pairingCode: String,
    private val onSubtitle: (Subtitle) -> Unit,
    private val onClients: (Int) -> Unit,
) : WebSocketServer(InetSocketAddress(port)) {

    private val authorized: MutableSet<WebSocket> =
        Collections.synchronizedSet(mutableSetOf<WebSocket>())

    init {
        isReuseAddr = true
        connectionLostTimeout = 10
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {}

    override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
        authorized.remove(conn)
        onClients(authorized.size)
    }

    override fun onMessage(conn: WebSocket, message: String) {
        when (val m = Protocol.parse(message)) {
            is Protocol.Message.Hello -> {
                if (m.code == pairingCode) {
                    authorized.add(conn)
                    conn.send(Protocol.ok())
                    onClients(authorized.size)
                } else {
                    conn.send(Protocol.deny())
                    conn.close()
                }
            }
            is Protocol.Message.Sub -> {
                if (authorized.contains(conn)) onSubtitle(m.subtitle)
            }
            else -> {}
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {}

    override fun onStart() {}
}
