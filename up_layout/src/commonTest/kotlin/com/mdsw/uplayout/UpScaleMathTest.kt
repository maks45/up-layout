package com.mdsw.uplayout

import kotlin.test.Test
import kotlin.test.assertEquals

class UpScaleMathTest {

    @Test
    fun scalesExplicitSizeUniformly() {
        assertEquals(Pair(360, 210), resolveScaledSize(240, 140, 240f, 140f, 1.5f))
    }

    @Test
    fun scalesDownExplicitSize() {
        assertEquals(Pair(120, 70), resolveScaledSize(240, 140, 240f, 140f, 0.5f))
    }

    @Test
    fun usesFallbackWhenSizeNull() {
        assertEquals(Pair(200, 100), resolveScaledSize(null, null, 100f, 50f, 2f))
    }

    @Test
    fun clampsToMinSize() {
        assertEquals(Pair(32, 32), resolveScaledSize(40, 40, 40f, 40f, 0.1f))
    }

    @Test
    fun ignoresNonPositiveZoom() {
        assertEquals(Pair(240, 140), resolveScaledSize(240, 140, 240f, 140f, 0f))
    }

    @Test
    fun snapsScaledSizeToStep() {
        assertEquals(Pair(212, 124), resolveScaledSize(210, 122, 210f, 122f, 1.01f, stepDp = 4))
    }

    @Test
    fun stepRespectsMinSize() {
        assertEquals(Pair(32, 32), resolveScaledSize(40, 40, 40f, 40f, 0.5f, stepDp = 4))
    }
}
