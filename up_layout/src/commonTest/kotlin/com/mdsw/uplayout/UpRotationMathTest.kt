package com.mdsw.uplayout

import kotlin.test.Test
import kotlin.test.assertEquals

class UpRotationMathTest {

    @Test
    fun addsDelta() {
        assertEquals(45f, resolveRotationDegrees(30f, 15f))
    }

    @Test
    fun wrapsOver360() {
        assertEquals(10f, resolveRotationDegrees(350f, 20f))
    }

    @Test
    fun wrapsNegative() {
        assertEquals(330f, resolveRotationDegrees(10f, -40f))
    }

    @Test
    fun zeroDeltaNormalizes() {
        assertEquals(0f, resolveRotationDegrees(720f, 0f))
    }

    @Test
    fun snapsToStep() {
        assertEquals(44f, resolveRotationDegrees(30f, 15f, stepDegrees = 4f))
    }

    @Test
    fun stepWrapsToZero() {
        assertEquals(0f, resolveRotationDegrees(359f, 0f, stepDegrees = 4f))
    }
}
