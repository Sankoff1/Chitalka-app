package com.chitalka.ui.readerview

import com.chitalka.theme.ThemeColors

/** Вставка тёмных стилей в `<head>` HTML главы перед загрузкой в WebView. */
fun injectDarkReaderHead(
    html: String,
    colors: ThemeColors,
): String {
    val block =
        """<style type="text/css" id="chitalka-reader-dark">
html{background:${colors.background}!important;color-scheme:dark;}
body{background:${colors.background}!important;color:${colors.text}!important;}
a{color:${colors.topBarText}!important;}
p,h1,h2,h3,h4,h5,h6,li,td,th,div,span,blockquote,figcaption,dd,dt,label{color:inherit!important;}
pre,code,samp,kbd{background:rgba(255,255,255,0.08)!important;color:inherit!important;}
table{color:inherit!important;}
</style>"""
    val headClose = Regex("</head>", RegexOption.IGNORE_CASE).find(html)
    return if (headClose != null) {
        html.substring(0, headClose.range.first) + block + html.substring(headClose.range.first)
    } else {
        "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/>$block</head><body>$html</body></html>"
    }
}
