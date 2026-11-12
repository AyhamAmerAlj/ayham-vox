package com.ayham.vox.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreTest {

    @Test
    fun mockTranslatesKnownLine() {
        val engine = MockTranslationEngine()
        assertEquals("احذر! خلفك!", engine.translate("Watch out behind you!"))
    }

    @Test
    fun mockMarksUnknownLine() {
        val engine = MockTranslationEngine()
        assertEquals("[تجريبي] Hello", engine.translate("Hello"))
    }

    @Test
    fun allSampleLinesAreTranslated() {
        val engine = MockTranslationEngine()
        engine.sampleLines.forEach {
            assertFalse(engine.translate(it).startsWith("[تجريبي]"))
        }
    }

    @Test
    fun deduperBlocksRepeatInsideWindowOnly() {
        val d = SubtitleDeduper(windowMs = 3000)
        assertFalse(d.isDuplicate("Hi", nowMs = 0))
        assertTrue(d.isDuplicate("hi ", nowMs = 1000))
        assertFalse(d.isDuplicate("Hi", nowMs = 5000))
    }

    @Test
    fun subtitleSurvivesProtocolRoundTrip() {
        val s = Subtitle(7, "Good job, team!", "أحسنتم يا فريق!", 123L)
        assertEquals(Protocol.Message.Sub(s), Protocol.parse(Protocol.subtitle(s)))
    }

    @Test
    fun helloAndGarbageParse() {
        assertEquals(Protocol.Message.Hello("123456"), Protocol.parse(Protocol.hello("123456")))
        assertEquals(Protocol.Message.Unknown, Protocol.parse("not json"))
    }
}
