package com.mdsw.uplayout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Dashed guides from each snapped item center to the container bound. */
@Composable
internal fun BoxScope.SnapGuidesOverlay(
    items: List<UpItem>,
    childRects: Map<String, Rect>,
    color: Color,
    border: BorderStroke? = null
) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    Canvas(modifier = Modifier.matchParentSize()) {
        val dash = PathEffect.dashPathEffect(UpDashIntervals, 0f)
        val strokeWidth = border?.width?.toPx() ?: 2f
        items.forEach { item ->
            val rect = childRects[item.id] ?: return@forEach
            if (rect == Rect.Zero) return@forEach
            val start = rect.center
            snapGuideDirections(item.alignment).forEach { direction ->
                val end = when (direction) {
                    UpSnapDirection.TOP -> Offset(start.x, 0f)
                    UpSnapDirection.BOTTOM -> Offset(start.x, size.height)
                    UpSnapDirection.START ->
                        if (isRtl) Offset(size.width, start.y) else Offset(0f, start.y)
                    UpSnapDirection.END ->
                        if (isRtl) Offset(0f, start.y) else Offset(size.width, start.y)
                }
                if (border == null) {
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        pathEffect = dash
                    )
                } else {
                    drawLine(
                        brush = border.brush,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        pathEffect = dash
                    )
                }
            }
        }
    }
}
