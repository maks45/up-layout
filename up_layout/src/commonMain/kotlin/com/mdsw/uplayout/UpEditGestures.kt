package com.mdsw.uplayout

import androidx.compose.ui.unit.IntSize

// Gesture policy shared by UpLayout edit mode:
// - container level owns two-finger scale/rotate so the second finger may land
//   outside the selected item bounds; single-finger events are not consumed;
// - item level owns press-to-select and single-finger drag; drop resolves to
//   alignment + padding via resolveDrop.
// First-intention locking per interaction lives in resolveTransformLock.

/** Fallback unscaled size in dp when no explicit size is set yet. */
internal fun fallbackItemSizeDp(
    sizePx: IntSize,
    padding: UpPadding,
    widthDp: Int?,
    heightDp: Int?,
    density: Float
): Pair<Float, Float> {
    val fallbackW = if (sizePx.width > 0) {
        sizePx.width / density - (padding.start + padding.end)
    } else {
        widthDp?.toFloat() ?: 100f
    }
    val fallbackH = if (sizePx.height > 0) {
        sizePx.height / density - (padding.top + padding.bottom)
    } else {
        heightDp?.toFloat() ?: 100f
    }
    return Pair(fallbackW, fallbackH)
}
