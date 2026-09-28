package com.mdsw.uplayout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpSnapMathTest {

    @Test
    fun centerHasNoGuides() {
        assertTrue(snapGuideDirections(UpAlignment.CENTER).isEmpty())
    }

    @Test
    fun edgeAlignmentsHaveSingleGuide() {
        assertEquals(setOf(UpSnapDirection.TOP), snapGuideDirections(UpAlignment.TOP))
        assertEquals(setOf(UpSnapDirection.BOTTOM), snapGuideDirections(UpAlignment.BOTTOM))
        assertEquals(setOf(UpSnapDirection.START), snapGuideDirections(UpAlignment.START))
        assertEquals(setOf(UpSnapDirection.END), snapGuideDirections(UpAlignment.END))
    }

    @Test
    fun cornerAlignmentsHaveTwoGuides() {
        assertEquals(
            setOf(UpSnapDirection.START, UpSnapDirection.TOP),
            snapGuideDirections(UpAlignment.START_TOP)
        )
        assertEquals(
            setOf(UpSnapDirection.END, UpSnapDirection.TOP),
            snapGuideDirections(UpAlignment.END_TOP)
        )
        assertEquals(
            setOf(UpSnapDirection.START, UpSnapDirection.BOTTOM),
            snapGuideDirections(UpAlignment.START_BOTTOM)
        )
        assertEquals(
            setOf(UpSnapDirection.END, UpSnapDirection.BOTTOM),
            snapGuideDirections(UpAlignment.END_BOTTOM)
        )
    }

    @Test
    fun unrotatedEdgeMidpointsSitOnBounds() {
        assertPointEquals(100f, 70f, edgeMidpoint(UpSnapDirection.TOP, 0f))
        assertPointEquals(100f, 130f, edgeMidpoint(UpSnapDirection.BOTTOM, 0f))
        assertPointEquals(50f, 100f, edgeMidpoint(UpSnapDirection.START, 0f))
        assertPointEquals(150f, 100f, edgeMidpoint(UpSnapDirection.END, 0f))
    }

    @Test
    fun quarterTurnSwapsAxes() {
        assertPointEquals(130f, 100f, edgeMidpoint(UpSnapDirection.TOP, 90f))
        assertPointEquals(70f, 100f, edgeMidpoint(UpSnapDirection.BOTTOM, 90f))
        assertPointEquals(100f, 50f, edgeMidpoint(UpSnapDirection.START, 90f))
        assertPointEquals(100f, 150f, edgeMidpoint(UpSnapDirection.END, 90f))
    }

    @Test
    fun halfTurnMirrorsToOppositeEdge() {
        assertPointEquals(100f, 130f, edgeMidpoint(UpSnapDirection.TOP, 180f))
        assertPointEquals(100f, 70f, edgeMidpoint(UpSnapDirection.BOTTOM, 180f))
    }

    @Test
    fun fullTurnMatchesNoRotation() {
        assertPointEquals(100f, 130f, edgeMidpoint(UpSnapDirection.BOTTOM, 360f))
        assertPointEquals(100f, 50f, edgeMidpoint(UpSnapDirection.END, -90f))
    }

    private fun edgeMidpoint(
        direction: UpSnapDirection,
        rotationDegrees: Float
    ): Pair<Float, Float> {
        return rotatedEdgeMidpoint(
            centerX = 100f,
            centerY = 100f,
            widthPx = 100f,
            heightPx = 60f,
            rotationDegrees = rotationDegrees,
            direction = direction
        )
    }

    private fun assertPointEquals(
        expectedX: Float,
        expectedY: Float,
        actual: Pair<Float, Float>
    ) {
        assertEquals(expectedX, actual.first, 0.01f)
        assertEquals(expectedY, actual.second, 0.01f)
    }
}
