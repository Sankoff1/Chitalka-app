package com.chitalka.ui.readerview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderBridgeMessagesTest {

    @Test
    fun parseScroll() {
        val m = parseReaderBridgeInboundMessage("""{"t":"scroll","y":12.5}""")
        assertTrue(m is ReaderBridgeInboundMessage.Scroll)
        val scroll = m as ReaderBridgeInboundMessage.Scroll
        assertEquals(12.5, scroll.y, 0.0)
        assertEquals(null, scroll.scrollRangeMax)
    }

    @Test
    fun parseScroll_withYMax() {
        val m = parseReaderBridgeInboundMessage("""{"t":"scroll","y":10,"yMax":800}""")
        assertTrue(m is ReaderBridgeInboundMessage.Scroll)
        val scroll = m as ReaderBridgeInboundMessage.Scroll
        assertEquals(10.0, scroll.y, 0.0)
        assertEquals(800.0, scroll.scrollRangeMax!!, 0.0)
    }

    @Test
    fun parseScroll_rejectsNonFinite() {
        assertNull(parseReaderBridgeInboundMessage("""{"t":"scroll","y":null}"""))
        assertNull(parseReaderBridgeInboundMessage("""{"t":"scroll"}"""))
    }

    @Test
    fun parsePage() {
        val prev = parseReaderBridgeInboundMessage("""{"t":"page","dir":"prev"}""")
        assertEquals(ReaderPageDirection.PREV, (prev as ReaderBridgeInboundMessage.Page).direction)
        val next = parseReaderBridgeInboundMessage("""{"t":"page","dir":"next"}""")
        assertEquals(ReaderPageDirection.NEXT, (next as ReaderBridgeInboundMessage.Page).direction)
        assertNull(parseReaderBridgeInboundMessage("""{"t":"page","dir":"up"}"""))
    }

    @Test
    fun parseReady() {
        assertTrue(parseReaderBridgeInboundMessage("""{"t":"ready"}""") is ReaderBridgeInboundMessage.Ready)
    }

    @Test
    fun parseMalformed_returnsNull() {
        assertNull(parseReaderBridgeInboundMessage("not-json"))
    }
}
