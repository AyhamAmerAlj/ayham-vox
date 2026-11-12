package com.ayham.vox.phone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** استماع مستمر عبر خدمة التعرف على الصوت في Android (إنكليزي). يعيد الاستماع تلقائياً بعد كل جملة. */
class SpeechInput(
    private val context: Context,
    private val onPartial: (String) -> Unit,
    private val onFinal: (String) -> Unit,
    private val onProblem: (String) -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    @Volatile private var active = false

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onProblem("خدمة التعرف على الصوت غير متوفرة على هذا الهاتف")
            return
        }
        active = true
        handler.post {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(listener)
                }
            }
            listen()
        }
    }

    fun stop() {
        active = false
        handler.removeCallbacksAndMessages(null)
        handler.post {
            recognizer?.destroy()
            recognizer = null
        }
    }

    private fun listen() {
        if (!active) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        recognizer?.startListening(intent)
    }

    private fun restart(delayMs: Long = 300) {
        if (!active) return
        handler.postDelayed({ listen() }, delayMs)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) onPartial(text)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) onFinal(text)
            restart()
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> restart()
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> restart(1000)
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    active = false
                    onProblem("صلاحية الميكروفون مرفوضة")
                }
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                    onProblem("لا يوجد إنترنت لخدمة التعرف على الصوت")
                    restart(2000)
                }
                else -> {
                    onProblem("خطأ في التعرف على الصوت (رمز $error)")
                    restart(1000)
                }
            }
        }
    }
}
