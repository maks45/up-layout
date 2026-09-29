package com.mdsw.uplayout

data class UpDropResult(
    val alignment: UpAlignment,
    val padding: UpPadding
)

/** Rounds [value] to the nearest multiple of [step]. Returns [value] when step <= 0. */
fun roundIntToStep(value: Int, step: Int): Int {
    if (step <= 0) return value
    return (value + step / 2) / step * step
}

@Deprecated(
    "Use roundIntToStep (renamed to avoid confusion with domain steps).",
    ReplaceWith("roundIntToStep(value, step)", "com.mdsw.uplayout.roundIntToStep")
)
fun roundToStep(value: Int, step: Int): Int = roundIntToStep(value, step)

// Pure port of IACreator's updateAlignmentAndPadding. All values are dp,
// child bounds are in container coordinates. No Compose types involved.
// centerDeadZoneDp is the full width/height of the CENTER snapping band.
fun resolveDrop(
    containerWidthDp: Float,
    containerHeightDp: Float,
    childLeftDp: Float,
    childTopDp: Float,
    childRightDp: Float,
    childBottomDp: Float,
    centerDeadZoneDp: Float = 20f,
    snapStepDp: Int = 20,
    snapToGrid: Boolean = true
): UpDropResult {
    val half = centerDeadZoneDp / 2f
    val hCenter = containerWidthDp / 2f
    val vCenter = containerHeightDp / 2f
    val centerX = (childLeftDp + childRightDp) / 2f
    val centerY = (childTopDp + childBottomDp) / 2f
    val hAlign = when {
        centerX < hCenter - half -> UpAlignment.START
        centerX > hCenter + half -> UpAlignment.END
        else -> UpAlignment.CENTER
    }
    val vAlign = when {
        centerY < vCenter - half -> UpAlignment.TOP
        centerY > vCenter + half -> UpAlignment.BOTTOM
        else -> UpAlignment.CENTER
    }
    val alignment = when {
        hAlign == UpAlignment.START && vAlign == UpAlignment.TOP -> UpAlignment.START_TOP
        hAlign == UpAlignment.CENTER && vAlign == UpAlignment.TOP -> UpAlignment.TOP
        hAlign == UpAlignment.END && vAlign == UpAlignment.TOP -> UpAlignment.END_TOP
        hAlign == UpAlignment.START && vAlign == UpAlignment.CENTER -> UpAlignment.START
        hAlign == UpAlignment.CENTER && vAlign == UpAlignment.CENTER -> UpAlignment.CENTER
        hAlign == UpAlignment.END && vAlign == UpAlignment.CENTER -> UpAlignment.END
        hAlign == UpAlignment.START -> UpAlignment.START_BOTTOM
        hAlign == UpAlignment.CENTER -> UpAlignment.BOTTOM
        else -> UpAlignment.END_BOTTOM
    }

    fun snapped(value: Float): Int {
        val intValue = value.coerceAtLeast(0f).toInt()
        return if (snapToGrid) roundIntToStep(intValue, snapStepDp) else intValue
    }
    val top = snapped(childTopDp)
    val bottom = snapped(containerHeightDp - childBottomDp)
    val start = snapped(childLeftDp)
    val end = snapped(containerWidthDp - childRightDp)
    val padding = when (alignment) {
        UpAlignment.START_TOP -> UpPadding(top = top, start = start)
        UpAlignment.TOP -> UpPadding(top = top)
        UpAlignment.END_TOP -> UpPadding(top = top, end = end)
        UpAlignment.START -> UpPadding(start = start)
        UpAlignment.CENTER -> UpPadding()
        UpAlignment.END -> UpPadding(end = end)
        UpAlignment.START_BOTTOM -> UpPadding(bottom = bottom, start = start)
        UpAlignment.BOTTOM -> UpPadding(bottom = bottom)
        UpAlignment.END_BOTTOM -> UpPadding(bottom = bottom, end = end)
    }
    return UpDropResult(alignment, padding)
}
