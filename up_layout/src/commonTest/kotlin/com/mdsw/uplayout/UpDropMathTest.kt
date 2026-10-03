package com.mdsw.uplayout

import kotlin.test.Test
import kotlin.test.assertEquals

class UpDropMathTest {

    @Test
    fun centeredChildResolvesToCenterWithZeroPadding() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 150f,
            childTopDp = 350f,
            childRightDp = 250f,
            childBottomDp = 450f,
            snapToGrid = false
        )
        assertEquals(UpAlignment.CENTER, result.alignment)
        assertEquals(UpPadding(), result.padding)
    }

    @Test
    fun topStartCornerResolvesToStartTop() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 10f,
            childTopDp = 10f,
            childRightDp = 110f,
            childBottomDp = 60f,
            snapToGrid = false
        )
        assertEquals(UpAlignment.START_TOP, result.alignment)
        assertEquals(UpPadding(top = 10, start = 10), result.padding)
    }

    @Test
    fun bottomEndCornerResolvesToEndBottom() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 290f,
            childTopDp = 740f,
            childRightDp = 390f,
            childBottomDp = 790f,
            snapToGrid = false
        )
        assertEquals(UpAlignment.END_BOTTOM, result.alignment)
        assertEquals(UpPadding(bottom = 10, end = 10), result.padding)
    }

    @Test
    fun snapToGridRoundsAllPaddings() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 12f,
            childTopDp = 12f,
            childRightDp = 112f,
            childBottomDp = 62f,
            snapStepDp = 20,
            snapToGrid = true
        )
        assertEquals(UpAlignment.START_TOP, result.alignment)
        assertEquals(UpPadding(top = 20, start = 20), result.padding)
    }

    @Test
    fun endPaddingIsNotRoundedWhenSnapDisabled() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 288f,
            childTopDp = 738f,
            childRightDp = 388f,
            childBottomDp = 788f,
            snapStepDp = 20,
            snapToGrid = false
        )
        assertEquals(UpAlignment.END_BOTTOM, result.alignment)
        assertEquals(UpPadding(bottom = 12, end = 12), result.padding)
    }

    @Test
    fun childOutsideContainerIsClampedToZero() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = -30f,
            childTopDp = -30f,
            childRightDp = 70f,
            childBottomDp = 20f,
            snapToGrid = false
        )
        assertEquals(UpAlignment.START_TOP, result.alignment)
        assertEquals(UpPadding(top = 0, start = 0), result.padding)
    }

    @Test
    fun lockedAlignmentKeepsAlignmentAndRecomputesItsPaddings() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 290f,
            childTopDp = 740f,
            childRightDp = 390f,
            childBottomDp = 790f,
            snapToGrid = false,
            lockedAlignment = UpAlignment.START_TOP
        )
        assertEquals(UpAlignment.START_TOP, result.alignment)
        assertEquals(UpPadding(top = 740, start = 290), result.padding)
    }

    @Test
    fun lockedCenterStaysCenterWithZeroPadding() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 10f,
            childTopDp = 10f,
            childRightDp = 110f,
            childBottomDp = 60f,
            snapToGrid = false,
            lockedAlignment = UpAlignment.CENTER
        )
        assertEquals(UpAlignment.CENTER, result.alignment)
        assertEquals(UpPadding(), result.padding)
    }

    @Test
    fun roundIntToStepRoundsToNearest() {
        assertEquals(20, roundIntToStep(12, 20))
        assertEquals(0, roundIntToStep(9, 20))
        assertEquals(40, roundIntToStep(30, 20))
    }

    @Test
    fun roundIntToStepReturnsValueForNonPositiveStep() {
        assertEquals(12, roundIntToStep(12, 0))
        assertEquals(12, roundIntToStep(12, -4))
    }

    @Test
    fun uniformZoneSetsBothAxes() {
        assertEquals(UpCenterZone(0.2f, 0.2f), UpCenterZone(0.2f))
    }

    @Test
    fun wideZoneSnapsOffCenterDropToCenter() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 100f,
            childTopDp = 250f,
            childRightDp = 200f,
            childBottomDp = 350f,
            centerZone = UpCenterZone(0.5f),
            snapToGrid = false
        )
        assertEquals(UpAlignment.CENTER, result.alignment)
    }

    @Test
    fun zeroZoneSnapsOnlyExactCenter() {
        val result = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 151f,
            childTopDp = 350f,
            childRightDp = 251f,
            childBottomDp = 450f,
            centerZone = UpCenterZone(0f),
            snapToGrid = false
        )
        assertEquals(UpAlignment.END, result.alignment)
    }

    @Test
    fun horizontalAndVerticalZonesApplyIndependently() {
        val zone = UpCenterZone(horizontal = 1f, vertical = 0f)
        val topEdge = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 10f,
            childTopDp = 10f,
            childRightDp = 110f,
            childBottomDp = 60f,
            centerZone = zone,
            snapToGrid = false
        )
        assertEquals(UpAlignment.TOP, topEdge.alignment)
        val middle = resolveDrop(
            containerWidthDp = 400f,
            containerHeightDp = 800f,
            childLeftDp = 10f,
            childTopDp = 350f,
            childRightDp = 110f,
            childBottomDp = 450f,
            centerZone = zone,
            snapToGrid = false
        )
        assertEquals(UpAlignment.CENTER, middle.alignment)
    }
}
