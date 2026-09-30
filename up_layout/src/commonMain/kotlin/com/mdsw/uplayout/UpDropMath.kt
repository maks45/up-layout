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
// When lockedAlignment is provided, the alignment is kept and only the
// paddings of that alignment are recomputed from the drop rect.
fun resolveDrop(
    containerWidthDp: Float,
    containerHeightDp: Float,
    childLeftDp: Float,
    childTopDp: Float,
    childRightDp: Float,
    childBottomDp: Float,
    centerDeadZoneDp: Float = 20f,
    snapStepDp: Int = 20,
    snapToGrid: Boolean = true,
    lockedAlignment: UpAlignment? = null
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
    val alignment = lockedAlignment ?: when (hAlign to vAlign) {
        UpAlignment.START to UpAlignment.TOP -> UpAlignment.START_TOP
        UpAlignment.CENTER to UpAlignment.TOP -> UpAlignment.TOP
        UpAlignment.END to UpAlignment.TOP -> UpAlignment.END_TOP
        UpAlignment.START to UpAlignment.CENTER -> UpAlignment.START
        UpAlignment.CENTER to UpAlignment.CENTER -> UpAlignment.CENTER
        UpAlignment.END to UpAlignment.CENTER -> UpAlignment.END
        UpAlignment.START to UpAlignment.BOTTOM -> UpAlignment.START_BOTTOM
        UpAlignment.CENTER to UpAlignment.BOTTOM -> UpAlignment.BOTTOM
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
