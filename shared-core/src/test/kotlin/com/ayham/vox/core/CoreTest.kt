package com.ayham.vox.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreTest {

    @Test
    fun mockTranslatesKnownLineInBothModes() {
        val engine = MockTranslationEngine()
        assertEquals("احذر! خلفك!", engine.translate("Watch out behind you!", DialectMode.FUSHA))
        assertEquals("دير بالك! وراك!", engine.translate("Watch out behind you!", DialectMode.IRAQI))
    }

    @Test
    fun mockMarksUnknownLine() {
        val engine = MockTranslationEngine()
        assertEquals("[تجريبي] Hello", engine.translate("Hello", DialectMode.FUSHA))
    }

    @Test
    fun allSampleLinesAreTranslated() {
        val engine = MockTranslationEngine()
        engine.sampleLines.forEach {
            assertFalse(engine.translate(it, DialectMode.IRAQI).startsWith("[تجريبي]"))
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
        val s = Subtitle(7, "Good job, team!", "عاشت ايديكم يا فريق!", DialectMode.IRAQI, 123L)
        val parsed = Protocol.parse(Protocol.subtitle(s))
        assertEquals(Protocol.Message.Sub(s), parsed)
    }

    @Test
    fun helloAndGarbageParse() {
        assertEquals(Protocol.Message.Hello("123456"), Protocol.parse(Protocol.hello("123456")))
        assertEquals(Protocol.Message.Unknown, Protocol.parse("not json"))
    }
}
