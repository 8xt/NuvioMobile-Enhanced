package com.nuvio.app.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import platform.UIKit.UIViewController
import kotlin.math.abs

/**
 * Back handling for the iPad trackpad two-finger swipe (see NuvioTrackpadBackGesture in
 * ContentView.swift). Every [PlatformBackHandler] on iOS registers an entry here, scoped to the
 * UIViewController that hosts it, so the Swift side can deliver a "back" to the screen under the
 * pointer exactly like Android's system back.
 */
internal class NuvioBackGestureEntry(var host: UIViewController?) {
    var enabled: Boolean = false
    var onBack: () -> Unit = {}
}

internal object NuvioBackGestureRegistry {
    private val entries = mutableListOf<NuvioBackGestureEntry>()

    /** Set when a horizontally scrollable child (row, pager…) consumed scroll during the gesture. */
    var horizontalScrollConsumed: Boolean = false

    fun register(entry: NuvioBackGestureEntry) {
        entries.remove(entry)
        entries.add(entry)
    }

    fun unregister(entry: NuvioBackGestureEntry) {
        entries.remove(entry)
    }

    fun dispatch(candidates: List<UIViewController>): Boolean {
        if (candidates.isEmpty()) return false
        val entry = entries.lastOrNull { entry ->
            val host = entry.host
            entry.enabled &&
                host != null &&
                host.viewIfLoaded?.window != null &&
                candidates.any { it == host }
        } ?: return false
        entry.onBack()
        return true
    }
}

private object NuvioHorizontalScrollTracker : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (abs(consumed.x) > 0.5f && abs(consumed.x) >= abs(consumed.y)) {
            NuvioBackGestureRegistry.horizontalScrollConsumed = true
        }
        return Offset.Zero
    }
}

/** Lets the trackpad back swipe yield to horizontal rows/pagers that are actually scrolling. */
internal fun Modifier.nuvioBackGestureScrollTracking(): Modifier = nestedScroll(NuvioHorizontalScrollTracker)

// region Swift bridge (NuvioBackGestureKt)

@Suppress("unused")
fun nuvioBackGestureBegan() {
    NuvioBackGestureRegistry.horizontalScrollConsumed = false
}

@Suppress("unused")
fun nuvioBackGestureBlockedByHorizontalScroll(): Boolean =
    NuvioBackGestureRegistry.horizontalScrollConsumed

@Suppress("unused")
fun isNuvioNativeBottomSheet(controller: UIViewController): Boolean =
    activeNuvioNativeBottomSheetForBackGesture() == controller

/**
 * Delivers one back event. [candidateControllers] are the view controllers under the pointer
 * (innermost first). Returns true when something in Compose handled it.
 */
@Suppress("unused")
fun dispatchNuvioBackGesture(candidateControllers: List<UIViewController>): Boolean {
    val sheet = activeNuvioNativeBottomSheetForBackGesture()
    if (sheet != null) {
        if (NuvioBackGestureRegistry.dispatch(listOf(sheet))) return true
        return dismissNativeNuvioBottomSheetFromBackGesture()
    }
    return NuvioBackGestureRegistry.dispatch(candidateControllers)
}

// endregion
