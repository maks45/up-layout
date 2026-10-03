package com.mdsw.uplayout

/** Scaled item size in dp. */
data class UpScaledSize(
    val widthDp: Int,
    val heightDp: Int
)

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
): UpScaledSize {
    val baseWidth = (currentWidthDp?.toFloat() ?: fallbackWidthDp).coerceAtLeast(minSizeDp.toFloat())
    val baseHeight = (currentHeightDp?.toFloat() ?: fallbackHeightDp).coerceAtLeast(minSizeDp.toFloat())
    if (zoom <= 0f || zoom == 1f) {
        return UpScaledSize(baseWidth.toInt(), baseHeight.toInt())
    }
    var newWidth = (baseWidth * zoom).toInt().coerceAtLeast(minSizeDp)
    var newHeight = (baseHeight * zoom).toInt().coerceAtLeast(minSizeDp)
    if (stepDp > 1) {
        newWidth = roundIntToStep(newWidth, stepDp).coerceAtLeast(minSizeDp)
        newHeight = roundIntToStep(newHeight, stepDp).coerceAtLeast(minSizeDp)
    }
    return UpScaledSize(newWidth, newHeight)
}
