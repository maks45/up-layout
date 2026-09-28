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
fun resolveTransformLock(
    accumZoomFactor: Float,
    accumRotationDegrees: Float,
    zoomThreshold: Float = 0.03f,
    rotationThresholdDegrees: Float = 3f
): UpTransformMode? {
    val zoomPassed = abs(accumZoomFactor - 1f) > zoomThreshold
    val rotationPassed = abs(accumRotationDegrees) > rotationThresholdDegrees
    if (zoomPassed && !rotationPassed) return UpTransformMode.SCALE
    if (rotationPassed && !zoomPassed) return UpTransformMode.ROTATE
    if (zoomPassed && rotationPassed) {
        val zoomScore = abs(accumZoomFactor - 1f) / zoomThreshold
        val rotationScore = abs(accumRotationDegrees) / rotationThresholdDegrees
        return if (rotationScore > zoomScore) UpTransformMode.ROTATE else UpTransformMode.SCALE
    }
    return null
}
