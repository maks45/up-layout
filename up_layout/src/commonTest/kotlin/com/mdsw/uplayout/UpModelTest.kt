package com.mdsw.uplayout

import kotlin.test.Test
import kotlin.test.assertEquals

class UpModelTest {

    @Test
    fun itemDefaultsToZeroRotation() {
        val item = UpItem(id = "a")
        assertEquals(0f, item.rotationDegrees)
    }

    @Test
    fun itemKeepsIdPaddingSizeAlignmentAndRotation() {
        val item = UpItem(
            id = "a",
            alignment = UpAlignment.START_TOP,
            padding = UpPadding(top = 16, start = 16),
            widthDp = 240,
            heightDp = 140,
            rotationDegrees = 15f
        )
        assertEquals("a", item.id)
        assertEquals(UpAlignment.START_TOP, item.alignment)
        assertEquals(UpPadding(top = 16, start = 16), item.padding)
        assertEquals(240, item.widthDp)
        assertEquals(140, item.heightDp)
        assertEquals(15f, item.rotationDegrees)
    }

    @Test
    fun screenConfigHoldsAllItems() {
        val config = UpScreenConfig(
            items = listOf(
                UpItem(id = "a"),
                UpItem(id = "b", rotationDegrees = 90f)
            )
        )
        assertEquals(listOf("a", "b"), config.items.map { it.id })
        assertEquals(90f, config.items[1].rotationDegrees)
    }

    @Test
    fun screenConfigDefaultsToEmpty() {
        assertEquals(emptyList(), UpScreenConfig().items)
    }
}
