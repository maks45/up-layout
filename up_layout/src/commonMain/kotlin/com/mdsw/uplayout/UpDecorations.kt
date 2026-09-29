package com.mdsw.uplayout

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val UpDashIntervals = floatArrayOf(10f, 10f)

/** Shared placement: alignment + bounds. Used by both view and edit modes. */
internal fun BoxScope.upPlacedItem(item: UpItem): Modifier =
    Modifier
        .align(item.alignment.toComposeAlignment())
        .upItemBounds(item)

internal fun Modifier.upItemBounds(item: UpItem): Modifier {
    return this
        .padding(
            top = item.padding.top.dp,
            bottom = item.padding.bottom.dp,
            start = item.padding.start.dp,
            end = item.padding.end.dp
        )
        .then(if (item.widthDp != null) Modifier.width(item.widthDp.dp) else Modifier)
        .then(if (item.heightDp != null) Modifier.height(item.heightDp.dp) else Modifier)
        .rotate(item.rotationDegrees)
}

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

internal fun Modifier.dashedBorder(color: Color) = this.drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(UpDashIntervals, 0f)
        )
    )
}

internal fun Modifier.dashedItemBorder(color: Color, width: Dp = 1.dp) = this.drawBehind {
    drawRect(
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(UpDashIntervals, 0f)
        )
    )
}

internal fun Modifier.drawAlignmentGrid(
    color: Color,
    centerSize: Dp,
    showAlignmentGrid: Boolean = true
) =
    if (!showAlignmentGrid) this
    else this.drawBehind {
        val horizontalDistance = size.width / 2f - centerSize.toPx() / 2
        val verticalDistance = size.height / 2f - centerSize.toPx() / 2

        drawLine(
            color = color,
            start = Offset(horizontalDistance, 0f),
            end = Offset(horizontalDistance, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = color,
            start = Offset(size.width - horizontalDistance, 0f),
            end = Offset(size.width - horizontalDistance, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = color,
            start = Offset(0f, verticalDistance),
            end = Offset(size.width, verticalDistance),
            strokeWidth = 1f
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height - verticalDistance),
            end = Offset(size.width, size.height - verticalDistance),
            strokeWidth = 1f
        )
    }

internal fun Modifier.drawSnapGrid(
    dotSpacing: Dp,
    showGrid: Boolean,
    color: Color = Color.Gray
): Modifier {
    if (!showGrid) return this
    return this.drawBehind {
        val spacingPx = dotSpacing.toPx()
        if (spacingPx <= 0f) return@drawBehind
        val xCount = (size.width / spacingPx).toInt()
        val yCount = (size.height / spacingPx).toInt()
        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                drawCircle(
                    color = color,
                    radius = 1f,
                    center = Offset(x * spacingPx, y * spacingPx)
                )
            }
        }
    }
}
