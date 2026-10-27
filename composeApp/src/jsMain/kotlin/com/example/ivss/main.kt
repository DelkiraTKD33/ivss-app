package com.example.ivss

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val webApp = document.getElementById("webApp") ?: document.body ?: return
    ComposeViewport(webApp) {
        App()
    }
}
