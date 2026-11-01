package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val host = LocalUIViewController.current
    val currentOnBack = rememberUpdatedState(onBack)
    val entry = remember { NuvioBackGestureEntry(host) }

    SideEffect {
        entry.host = host
        entry.enabled = enabled
    }

    DisposableEffect(entry) {
        entry.onBack = { currentOnBack.value() }
        NuvioBackGestureRegistry.register(entry)
        onDispose { NuvioBackGestureRegistry.unregister(entry) }
    }
}
