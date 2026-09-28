package com.mdsw.uplayout

import kotlinx.serialization.Serializable

@Serializable
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

@Serializable
data class UpPadding(
    val top: Int = 0,
    val bottom: Int = 0,
    val start: Int = 0,
    val end: Int = 0
)

@Serializable
data class UpItem(
    val id: String,
    val alignment: UpAlignment = UpAlignment.CENTER,
    val padding: UpPadding = UpPadding(),
    val widthDp: Int? = null,
    val heightDp: Int? = null,
    val rotationDegrees: Float = 0f
)

@Serializable
data class UpScreenConfig(
    val frames: List<UpItem> = emptyList()
)

data class UpGridSettings(
    val showGrid: Boolean = true,
    val snapToGrid: Boolean = true,
    val snapGridSize: Int = 20,
    val visibleGridSize: Int = 40,
    val rotationStepDegrees: Float = 4f,
    val scaleStepDp: Int = 4
)
