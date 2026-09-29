package com.mdsw.uplayout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Logical anchor of an item inside the container. */
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

/** Container-relative offsets in dp applied after alignment. */
@Serializable
data class UpPadding(
    val top: Int = 0,
    val bottom: Int = 0,
    val start: Int = 0,
    val end: Int = 0
)

/** A single manipulable child. Width/height null means wrap content. */
@Serializable
data class UpItem(
    val id: String,
    val alignment: UpAlignment = UpAlignment.CENTER,
    val padding: UpPadding = UpPadding(),
    val widthDp: Int? = null,
    val heightDp: Int? = null,
    val rotationDegrees: Float = 0f
)

/** Persisted screen configuration holding all items. */
@Serializable
data class UpScreenConfig(
    @SerialName("frames") val items: List<UpItem> = emptyList()
)

/** Edit-mode configuration: grid rendering, snapping steps and transform locks. */
data class UpEditSettings(
    val showGrid: Boolean = true,
    val showSnapGuides: Boolean = true,
    val snapToGrid: Boolean = true,
    val snapStepDp: Int = 20,
    val visibleStepDp: Int = 40,
    val rotationStepDegrees: Float = 4f,
    val scaleStepDp: Int = 4,
    val lockMove: Boolean = false,
    val lockRotation: Boolean = false,
    val lockScale: Boolean = false
)

@Deprecated(
    "Use UpEditSettings (renamed for scope clarity).",
    ReplaceWith("UpEditSettings", "com.mdsw.uplayout.UpEditSettings")
)
typealias UpGridSettings = UpEditSettings
