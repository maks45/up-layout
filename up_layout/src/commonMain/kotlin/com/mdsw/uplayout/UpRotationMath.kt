package com.mdsw.uplayout

import kotlin.math.round

// Pure rotation math for two-finger twist. Degrees, normalized to [0, 360).
// When stepDegrees > 0 the result snaps to the nearest step multiple.
fun resolveRotationDegrees(
    currentDegrees: Float,
    deltaDegrees: Float,
    stepDegrees: Float = 0f
): Float {
    val normalized = normalizeRotation(currentDegrees + deltaDegrees)
    if (stepDegrees > 0f) {
        val snapped = round(normalized / stepDegrees) * stepDegrees
        return normalizeRotation(snapped)
    }
    return normalized
}

private fun normalizeRotation(degrees: Float): Float {
    return ((degrees % 360f) + 360f) % 360f
}
