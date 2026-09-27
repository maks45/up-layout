package com.mdsw.uplayout

enum class UpAlignment {
    CENTER,
    TOP,
    BOTTOM,
    START,
    END,
    START_TOP,
    START_BOTTOM,
    END_TOP,
    END_BOTTOM
}

data class UpPadding(
    val top: Int = 0,
    val bottom: Int = 0,
    val start: Int = 0,
    val end: Int = 0
)

data class UpItem(
    val id: String,
    val alignment: UpAlignment = UpAlignment.CENTER,
    val padding: UpPadding = UpPadding(),
    val widthDp: Int? = null,
    val heightDp: Int? = null
)

data class UpGridSettings(
    val showGrid: Boolean = true,
    val snapToGrid: Boolean = true,
    val snapGridSize: Int = 20,
    val visibleGridSize: Int = 40
)
