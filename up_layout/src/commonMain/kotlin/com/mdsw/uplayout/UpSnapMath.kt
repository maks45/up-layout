package com.mdsw.uplayout

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Pure snap-guide math: which container bounds an alignment is snapped to,
// and where on the (possibly rotated) frame edge the guide starts.
// Keeps direction/geometry logic platform-independent so it stays covered by
// commonTest. Rendering (dashed line from frame edge to bound) lives in UpLayout.
enum class UpSnapDirection {
    TOP,
    BOTTOM,
    START,
    END
}

fun snapGuideDirections(alignment: UpAlignment): Set<UpSnapDirection> {
    return when (alignment) {
        UpAlignment.CENTER -> emptySet()
        UpAlignment.TOP -> setOf(UpSnapDirection.TOP)
        UpAlignment.BOTTOM -> setOf(UpSnapDirection.BOTTOM)
        UpAlignment.START -> setOf(UpSnapDirection.START)
        UpAlignment.END -> setOf(UpSnapDirection.END)
        UpAlignment.START_TOP -> setOf(UpSnapDirection.START, UpSnapDirection.TOP)
        UpAlignment.END_TOP -> setOf(UpSnapDirection.END, UpSnapDirection.TOP)
        UpAlignment.START_BOTTOM -> setOf(UpSnapDirection.START, UpSnapDirection.BOTTOM)
        UpAlignment.END_BOTTOM -> setOf(UpSnapDirection.END, UpSnapDirection.BOTTOM)
    }
}

// Visual edge midpoint of a frame rotated by rotationDegrees about its center.
// Center is the AABB center (= rotation center for Modifier.rotate); width/height
// are the unrotated layout-box size in px. Returns the (x, y) start point for the
// guide in the given direction. Rotation needs no normalization: sin/cos accept
// any angle, so unwrapped values work as-is.
fun rotatedEdgeMidpoint(
    centerX: Float,
    centerY: Float,
    widthPx: Float,
    heightPx: Float,
    rotationDegrees: Float,
    direction: UpSnapDirection
): Pair<Float, Float> {
    val (localX, localY) = when (direction) {
        UpSnapDirection.TOP -> Pair(0f, -heightPx / 2f)
        UpSnapDirection.BOTTOM -> Pair(0f, heightPx / 2f)
        UpSnapDirection.START -> Pair(-widthPx / 2f, 0f)
        UpSnapDirection.END -> Pair(widthPx / 2f, 0f)
    }
    val radians = rotationDegrees * PI / 180.0
    val cosA = cos(radians).toFloat()
    val sinA = sin(radians).toFloat()
    return Pair(
        centerX + localX * cosA - localY * sinA,
        centerY + localX * sinA + localY * cosA
    )
}
