package com.mdsw.uplayout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val UpDashIntervals = floatArrayOf(10f, 10f)

/** Shared placement: alignment + bounds. Used by both view and edit modes. */
internal fun BoxScope.upPlacedItem(item: UpItem): Modifier =
    Modifier
        .align(item.alignment.toComposeAlignment())
        .upItemBounds(item)

internal fun Modifier.upItemBounds(item: UpItem): Modifier = padding(
    top = item.padding.top.dp,
    bottom = item.padding.bottom.dp,
    start = item.padding.start.dp,
    end = item.padding.end.dp
)
    .then(item.widthDp?.let { Modifier.width(it.dp) } ?: Modifier)
    .then(item.heightDp?.let { Modifier.height(it.dp) } ?: Modifier)
    .rotate(item.rotationDegrees)

/** Frame border for edit mode: selected solid, unselected dashed, or hidden. */
internal fun Modifier.upFrameBorder(
    isSelected: Boolean,
    settings: UpEditSettings,
    selectedColor: Color
): Modifier {
    if (!settings.showFrameBounds) return this
    if (isSelected) {
        return this.then(settings.selectedFrameBorder?.let { Modifier.border(it) }
            ?: Modifier.border(2.dp, selectedColor))
    }
    return this.then(settings.frameBorder?.let { dashedBorderStroke(it) }
        ?: dashedItemBorder(Color.Gray))
}

/** Container background + edit-mode grids. */
internal fun Modifier.upEditContainer(
    settings: UpEditSettings,
    centerSize: Dp,
    backgroundColor: Color,
    primary: Color
): Modifier = this
    .background(color = backgroundColor)
    .dashedBorder(primary)
    .drawAlignmentGrid(primary, centerSize, settings.showAlignmentGrid, settings.alignmentGridBorder)
    .drawSnapGrid(settings.visibleStepDp.dp, settings.showGrid, border = settings.gridBorder)

internal fun UpAlignment.toComposeAlignment(): Alignment {
    return when (this) {
        UpAlignment.CENTER -> Alignment.Center
        UpAlignment.TOP -> Alignment.TopCenter
        UpAlignment.BOTTOM -> Alignment.BottomCenter
        UpAlignment.START -> Alignment.CenterStart
        UpAlignment.END -> Alignment.CenterEnd
        UpAlignment.START_TOP -> Alignment.TopStart
        UpAlignment.END_TOP -> Alignment.TopEnd
        UpAlignment.START_BOTTOM -> Alignment.BottomStart
        UpAlignment.END_BOTTOM -> Alignment.BottomEnd
    }
}

internal fun Modifier.dashedBorder(color: Color) = dashedRect(SolidColor(color), null, 2f)

internal fun Modifier.dashedItemBorder(color: Color, width: Dp = 1.dp) =
    dashedRect(SolidColor(color), width)

/** Dashed rect using the width + brush of an existing [BorderStroke]. */
internal fun Modifier.dashedBorderStroke(border: BorderStroke) =
    dashedRect(border.brush, border.width)

private fun Modifier.dashedRect(
    brush: Brush,
    width: Dp?,
    widthPxFallback: Float = 1f
) = drawBehind {
    drawRect(
        brush = brush,
        style = Stroke(
            width?.toPx() ?: widthPxFallback,
            pathEffect = PathEffect.dashPathEffect(UpDashIntervals, 0f)
        )
    )
}

internal fun Modifier.drawAlignmentGrid(
    color: Color,
    centerSize: Dp,
    showAlignmentGrid: Boolean = true,
    border: BorderStroke? = null
) =
    if (!showAlignmentGrid) this
    else drawBehind {
        val brush = border?.brush ?: SolidColor(color)
        val strokeWidth = border?.width?.toPx() ?: 1f
        val x = size.width / 2f - centerSize.toPx() / 2
        val y = size.height / 2f - centerSize.toPx() / 2
        fun gridLine(start: Offset, end: Offset) = drawLine(brush, start, end, strokeWidth)

        gridLine(Offset(x, 0f), Offset(x, size.height))
        gridLine(Offset(size.width - x, 0f), Offset(size.width - x, size.height))
        gridLine(Offset(0f, y), Offset(size.width, y))
        gridLine(Offset(0f, size.height - y), Offset(size.width, size.height - y))
    }

internal fun Modifier.drawSnapGrid(
    dotSpacing: Dp,
    showGrid: Boolean,
    color: Color = Color.Gray,
    border: BorderStroke? = null
): Modifier {
    if (!showGrid) return this
    return drawBehind {
        val spacingPx = dotSpacing.toPx()
        if (spacingPx <= 0f) return@drawBehind
        val brush = border?.brush ?: SolidColor(color)
        val dotRadius = border?.width?.toPx()?.div(2f) ?: 1f
        val xCount = (size.width / spacingPx).toInt()
        val yCount = (size.height / spacingPx).toInt()
        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                drawCircle(brush, dotRadius, Offset(x * spacingPx, y * spacingPx))
            }
        }
    }
}
