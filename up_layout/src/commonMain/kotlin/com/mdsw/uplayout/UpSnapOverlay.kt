package com.mdsw.uplayout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

/** Dashed guides from each snapped item edge to the container bound. */
@Composable
internal fun BoxScope.SnapGuidesOverlay(
    items: List<UpItem>,
    childRects: Map<String, Rect>,
    childSizes: Map<String, IntSize>,
    color: Color
) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    Canvas(modifier = Modifier.matchParentSize()) {
        val dash = PathEffect.dashPathEffect(UpDashIntervals, 0f)
        items.forEach { item ->
            val rect = childRects[item.id] ?: return@forEach
            if (rect == Rect.Zero) return@forEach
            val center = rect.center
            val sizePx = childSizes[item.id]
            snapGuideDirections(item.alignment).forEach { direction ->
                val start = if (sizePx != null && sizePx.width > 0 && sizePx.height > 0) {
                    val (startX, startY) = rotatedEdgeMidpoint(
                        centerX = center.x,
                        centerY = center.y,
                        widthPx = sizePx.width.toFloat(),
                        heightPx = sizePx.height.toFloat(),
                        rotationDegrees = item.rotationDegrees,
                        direction = direction,
                        isRtl = isRtl
                    )
                    Offset(startX, startY)
                } else {
                    center
                }
                val end = when (direction) {
                    UpSnapDirection.TOP -> Offset(start.x, 0f)
                    UpSnapDirection.BOTTOM -> Offset(start.x, size.height)
                    UpSnapDirection.START ->
                        if (isRtl) Offset(size.width, start.y) else Offset(0f, start.y)
                    UpSnapDirection.END ->
                        if (isRtl) Offset(0f, start.y) else Offset(size.width, start.y)
                }
                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = 2f,
                    pathEffect = dash
                )
            }
        }
    }
}
