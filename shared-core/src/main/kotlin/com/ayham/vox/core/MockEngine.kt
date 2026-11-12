package com.ayham.vox.core

/** محرك تجريبي بدون أي API: قاموس صغير من جمل الألعاب (فصحى فقط). */
class MockTranslationEngine {

    private val dictionary: Map<String, String> = linkedMapOf(
        "watch out behind you!" to "احذر! خلفك!",
        "we need to get out of here." to "يجب أن نخرج من هنا.",
        "reload and cover me." to "أعد التعبئة وغطّني.",
        "enemy spotted on the left." to "تم رصد عدو على اليسار.",
        "good job, team!" to "أحسنتم يا فريق!",
        "heal me, i'm low on health." to "عالجني، صحتي منخفضة.",
        "let's push the objective." to "لنتقدم نحو الهدف.",
        "mission complete." to "اكتملت المهمة.",
    )

    /** الجمل الإنكليزية التي يدور عليها وضع التجربة. */
    val sampleLines: List<String> = listOf(
        "Watch out behind you!",
        "We need to get out of here.",
        "Reload and cover me.",
        "Enemy spotted on the left.",
        "Good job, team!",
        "Heal me, I'm low on health.",
        "Let's push the objective.",
        "Mission complete.",
    )

    fun translate(text: String): String =
        dictionary[text.trim().lowercase()] ?: "[تجريبي] $text"
}

/** يمنع تكرار نفس الترجمة خلال نافذة زمنية قصيرة. */
class SubtitleDeduper(private val windowMs: Long = 3000) {
    private var lastText: String? = null
    private var lastTimeMs: Long = 0

    fun isDuplicate(text: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        val normalized = text.trim().lowercase()
        val duplicate = normalized == lastText && nowMs - lastTimeMs < windowMs
        if (!duplicate) {
            lastText = normalized
            lastTimeMs = nowMs
        }
        return duplicate
    }
}
