package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable
import com.nuvio.app.isIos

/**
 * Back handler that only exists on iOS (iPad trackpad two-finger swipe back). Used for dialogs,
 * menus and popups, which Android already closes with its own system back.
 */
@Composable
internal fun IosBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
) {
    if (isIos) {
        PlatformBackHandler(enabled = enabled, onBack = onBack)
    }
}
