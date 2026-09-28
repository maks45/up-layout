package com.mdsw.uplayout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UpTransformLockTest {

    @Test
    fun undecidedBelowThresholds() {
        assertNull(resolveTransformLock(1.01f, 1f))
    }

    @Test
    fun locksScaleWhenZoomPasses() {
        assertEquals(UpTransformMode.SCALE, resolveTransformLock(1.05f, 1f))
    }

    @Test
    fun locksRotateWhenRotationPasses() {
        assertEquals(UpTransformMode.ROTATE, resolveTransformLock(1.01f, 5f))
    }

    @Test
    fun picksDominantWhenBothPass() {
        assertEquals(UpTransformMode.SCALE, resolveTransformLock(1.1f, 4f))
        assertEquals(UpTransformMode.ROTATE, resolveTransformLock(1.04f, 12f))
    }
}
