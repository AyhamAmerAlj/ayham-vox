package com.ayham.vox.core

/** واجهة محرك الترجمة. في V0.2 نضيف مزودين حقيقيين بنفس الواجهة. */
interface TranslationEngine {
    fun translate(text: String, mode: DialectMode): String
}

/** محرك تجريبي بدون أي API: قاموس صغير من جمل الألعاب. */
class MockTranslationEngine : TranslationEngine {

    private data class Entry(val fusha: String, val iraqi: String)

    private val dictionary: Map<String, Entry> = linkedMapOf(
        "watch out behind you!" to Entry("احذر! خلفك!", "دير بالك! وراك!"),
        "we need to get out of here." to Entry("يجب أن نخرج من هنا.", "لازم نطلع من هنا."),
        "reload and cover me." to Entry("أعد التعبئة وغطّني.", "عبّي وغطيلي."),
        "enemy spotted on the left." to Entry("تم رصد عدو على اليسار.", "شفت عدو على اليسار."),
        "good job, team!" to Entry("أحسنتم يا فريق!", "عاشت ايديكم يا فريق!"),
        "heal me, i'm low on health." to Entry("عالجني، صحتي منخفضة.", "عالجني، صحتي قليلة."),
        "let's push the objective." to Entry("لنتقدم نحو الهدف.", "يلا نتقدم للهدف."),
        "mission complete." to Entry("اكتملت المهمة.", "خلصت المهمة."),
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

    override fun translate(text: String, mode: DialectMode): String {
        val entry = dictionary[text.trim().lowercase()] ?: return "[تجريبي] $text"
        return if (mode == DialectMode.IRAQI) entry.iraqi else entry.fusha
    }
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
