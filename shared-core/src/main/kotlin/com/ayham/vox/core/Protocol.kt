package com.ayham.vox.core

import org.json.JSONObject

/** رسائل JSON بسيطة بين الهاتف والتلفزيون. */
object Protocol {
    const val PORT = 8765

    sealed class Message {
        data class Hello(val code: String) : Message()
        data class Sub(val subtitle: Subtitle) : Message()
        object Ok : Message()
        object Deny : Message()
        object Unknown : Message()
    }

    fun hello(code: String): String =
        JSONObject().put("t", "hello").put("code", code).toString()

    fun subtitle(s: Subtitle): String =
        JSONObject()
            .put("t", "sub")
            .put("id", s.id)
            .put("orig", s.original)
            .put("tr", s.translated)
            .put("mode", s.mode.name)
            .put("ts", s.timeMs)
            .toString()

    fun ok(): String = JSONObject().put("t", "ok").toString()

    fun deny(): String = JSONObject().put("t", "deny").toString()

    fun parse(text: String): Message {
        return try {
            val o = JSONObject(text)
            when (o.optString("t")) {
                "hello" -> Message.Hello(o.optString("code"))
                "sub" -> Message.Sub(
                    Subtitle(
                        id = o.optLong("id"),
                        original = o.optString("orig"),
                        translated = o.optString("tr"),
                        mode = runCatching { DialectMode.valueOf(o.optString("mode")) }
                            .getOrDefault(DialectMode.FUSHA),
                        timeMs = o.optLong("ts"),
                    )
                )
                "ok" -> Message.Ok
                "deny" -> Message.Deny
                else -> Message.Unknown
            }
        } catch (e: Exception) {
            Message.Unknown
        }
    }
}
