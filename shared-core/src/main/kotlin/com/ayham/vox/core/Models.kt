package com.ayham.vox.core

data class Subtitle(
    val id: Long,
    val original: String,
    val translated: String,
    val timeMs: Long,
)
