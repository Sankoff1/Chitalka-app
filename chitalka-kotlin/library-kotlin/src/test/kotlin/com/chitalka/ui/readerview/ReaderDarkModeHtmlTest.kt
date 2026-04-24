package com.chitalka.ui.readerview

import com.chitalka.theme.darkThemeColors
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderDarkModeHtmlTest {

    @Test
    fun injectBeforeHead() {
        val html = "<html><head><meta/></head><body><p>x</p></body></html>"
        val out = injectDarkReaderHead(html, darkThemeColors)
        assertTrue(out.contains("id=\"chitalka-reader-dark\""))
        assertTrue(out.contains(darkThemeColors.background))
        assertTrue(out.indexOf("chitalka-reader-dark") < out.indexOf("</head>"))
    }

    @Test
    fun injectWrapsWhenNoHead() {
        val frag = "<p>only</p>"
        val out = injectDarkReaderHead(frag, darkThemeColors)
        assertTrue(out.startsWith("<!DOCTYPE html>"))
        assertTrue(out.contains(frag))
        assertTrue(out.contains("chitalka-reader-dark"))
    }
}
