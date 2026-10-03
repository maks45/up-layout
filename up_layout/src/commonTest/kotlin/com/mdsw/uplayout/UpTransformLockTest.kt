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

    @Test
    fun lockedModeNeverWins() {
        assertNull(resolveTransformLock(1.05f, 1f, scaleLocked = true))
        assertNull(resolveTransformLock(1.01f, 5f, rotationLocked = true))
    }

    @Test
    fun fallsBackToUnlockedMode() {
        assertEquals(UpTransformMode.ROTATE, resolveTransformLock(1.1f, 4f, scaleLocked = true))
        assertEquals(UpTransformMode.SCALE, resolveTransformLock(1.04f, 12f, rotationLocked = true))
    }

    @Test
    fun bothLockedStaysUndecided() {
        assertNull(resolveTransformLock(1.5f, 45f, scaleLocked = true, rotationLocked = true))
    }
}
