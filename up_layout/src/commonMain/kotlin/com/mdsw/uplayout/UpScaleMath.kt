package com.mdsw.uplayout

// Pure scale math for pinch resize. All values are dp.
// Keeps transformation logic platform-independent so it stays covered by commonTest.
fun resolveScaledSize(
    currentWidthDp: Int?,
    currentHeightDp: Int?,
    fallbackWidthDp: Float,
    fallbackHeightDp: Float,
    zoom: Float,
    minSizeDp: Int = 32,
    stepDp: Int = 0
): Pair<Int, Int> {
    val baseWidth = (currentWidthDp?.toFloat() ?: fallbackWidthDp).coerceAtLeast(minSizeDp.toFloat())
    val baseHeight = (currentHeightDp?.toFloat() ?: fallbackHeightDp).coerceAtLeast(minSizeDp.toFloat())
    if (zoom <= 0f || zoom == 1f) {
        return Pair(baseWidth.toInt(), baseHeight.toInt())
    }
    var newWidth = (baseWidth * zoom).toInt().coerceAtLeast(minSizeDp)
    var newHeight = (baseHeight * zoom).toInt().coerceAtLeast(minSizeDp)
    if (stepDp > 1) {
        newWidth = roundToStep(newWidth, stepDp).coerceAtLeast(minSizeDp)
        newHeight = roundToStep(newHeight, stepDp).coerceAtLeast(minSizeDp)
    }
    return Pair(newWidth, newHeight)
}
