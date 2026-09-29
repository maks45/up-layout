package com.mdsw.uplayout

import kotlin.math.abs

// First-intention lock for two-finger gestures: once scaling starts the
// interaction stays scaling, once rotation starts it stays rotation.
enum class UpTransformMode {
    SCALE,
    ROTATE
}

// Accumulated zoom is the product of per-event zoom deltas (starts at 1f).
// Accumulated rotation is the sum of per-event rotation deltas in degrees.
// A locked mode never passes its threshold, so it can never win: with one lock
// the gesture falls back to the other mode once it passes, with both locks the
// result stays null. Defaults are unlocked so existing callers keep working.
/**
 * First-intention lock for a two-finger gesture. Returns the winning
 * [UpTransformMode], or null while undecided / when both modes are locked.
 */
fun resolveTransformLock(
    accumZoomFactor: Float,
    accumRotationDegrees: Float,
    zoomThreshold: Float = 0.03f,
    rotationThresholdDegrees: Float = 3f,
    scaleLocked: Boolean = false,
    rotationLocked: Boolean = false
): UpTransformMode? {
    val zoomPassed = !scaleLocked && abs(accumZoomFactor - 1f) > zoomThreshold
    val rotationPassed = !rotationLocked && abs(accumRotationDegrees) > rotationThresholdDegrees
    if (zoomPassed && !rotationPassed) return UpTransformMode.SCALE
    if (rotationPassed && !zoomPassed) return UpTransformMode.ROTATE
    if (zoomPassed && rotationPassed) {
        val zoomScore = abs(accumZoomFactor - 1f) / zoomThreshold
        val rotationScore = abs(accumRotationDegrees) / rotationThresholdDegrees
        return if (rotationScore > zoomScore) UpTransformMode.ROTATE else UpTransformMode.SCALE
    }
    return null
}
