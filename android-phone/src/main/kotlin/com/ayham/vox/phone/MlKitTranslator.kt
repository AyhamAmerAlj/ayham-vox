package com.ayham.vox.phone

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/** ترجمة إنكليزي ← عربي فصحى داخل الهاتف (ML Kit). النموذج ينزل مرة واحدة ثم يشتغل بدون إنترنت. */
class MlKitTranslator {

    private val translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.ARABIC)
            .build()
    )

    suspend fun prepare() {
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
    }

    suspend fun translate(text: String): String = translator.translate(text).await()

    fun close() = translator.close()
}
