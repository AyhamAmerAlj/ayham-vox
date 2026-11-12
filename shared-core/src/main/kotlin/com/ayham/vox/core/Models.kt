package com.ayham.vox.core

enum class DialectMode(val label: String) {
    FUSHA("العربية الفصحى"),
    IRAQI("اللهجة العراقية"),
}

data class Subtitle(
    val id: Long,
    val original: String,
    val translated: String,
    val mode: DialectMode,
    val timeMs: Long,
)
