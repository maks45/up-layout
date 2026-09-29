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
        UpAlignment.TOP -> TopGuides
        UpAlignment.BOTTOM -> BottomGuides
        UpAlignment.START -> StartGuides
        UpAlignment.END -> EndGuides
        UpAlignment.START_TOP -> StartTopGuides
        UpAlignment.END_TOP -> EndTopGuides
        UpAlignment.START_BOTTOM -> StartBottomGuides
        UpAlignment.END_BOTTOM -> EndBottomGuides
    }
}

private val TopGuides = setOf(UpSnapDirection.TOP)
private val BottomGuides = setOf(UpSnapDirection.BOTTOM)
private val StartGuides = setOf(UpSnapDirection.START)
private val EndGuides = setOf(UpSnapDirection.END)
private val StartTopGuides = setOf(UpSnapDirection.START, UpSnapDirection.TOP)
private val EndTopGuides = setOf(UpSnapDirection.END, UpSnapDirection.TOP)
private val StartBottomGuides = setOf(UpSnapDirection.START, UpSnapDirection.BOTTOM)
private val EndBottomGuides = setOf(UpSnapDirection.END, UpSnapDirection.BOTTOM)

// Visual edge midpoint of a frame rotated by rotationDegrees about its center.
// Center is the AABB center (= rotation center for Modifier.rotate); width/height
// are the unrotated layout-box size in px. Returns the (x, y) start point for the
// guide in the given direction. Rotation needs no normalization: sin/cos accept
// any angle, so unwrapped values work as-is. Note: UpItem.rotationDegrees storage
// is always normalized to [0, 360) by resolveRotationDegrees; geometry still
// accepts any float.
// START/END are logical (layout-direction aware): pass isRtl = true so the guide
// starts from the physical right edge for START in RTL layouts.
fun rotatedEdgeMidpoint(
    centerX: Float,
    centerY: Float,
    widthPx: Float,
    heightPx: Float,
    rotationDegrees: Float,
    direction: UpSnapDirection,
    isRtl: Boolean = false
): Pair<Float, Float> {
    val logicalDirection = if (isRtl) {
        when (direction) {
            UpSnapDirection.START -> UpSnapDirection.END
            UpSnapDirection.END -> UpSnapDirection.START
            else -> direction
        }
    } else {
        direction
    }
    val (localX, localY) = when (logicalDirection) {
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
