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
    fun fallback(sizePx: Int, padSum: Int, explicitDp: Int?): Float =
        if (sizePx > 0) sizePx / density - padSum else explicitDp?.toFloat() ?: 100f
    return Pair(
        fallback(sizePx.width, padding.start + padding.end, widthDp),
        fallback(sizePx.height, padding.top + padding.bottom, heightDp)
    )
}
