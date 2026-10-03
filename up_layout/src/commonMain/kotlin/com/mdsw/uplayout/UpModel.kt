package com.mdsw.uplayout

import androidx.compose.foundation.BorderStroke
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

/**
 * CENTER snapping zone as fractions (0..1) of the container size.
 * A drop whose center falls inside the zone snaps to CENTER on that axis.
 * [horizontal] is the zone width relative to the container width,
 * [vertical] the zone height relative to the container height.
 * `UpCenterZone(all)` sets both axes at once; [Default] is 5%.
 */
data class UpCenterZone(
    val horizontal: Float,
    val vertical: Float
) {
    constructor(all: Float) : this(all, all)

    companion object {
        val Default = UpCenterZone(0.05f)
    }
}

/** Edit-mode configuration: grid rendering, snapping steps and transform locks. */
data class UpEditSettings(
    val showGrid: Boolean = true,
    val showAlignmentGrid: Boolean = true,
    val showSnapGuides: Boolean = true,
    val showFrameBounds: Boolean = true,
    /**
     * Line styles (width + brush color) for edit-mode affordances.
     * Null keeps the built-in default: unselected frame is a dashed gray
     * `1.dp` rect, selected frame a solid theme-primary `2.dp` border,
     * snap guides dashed theme-primary lines, alignment grid solid
     * theme-primary lines, dot grid gray dots.
     * For [gridBorder] the brush is the dot color and the width is the dot
     * diameter.
     */
    val frameBorder: BorderStroke? = null,
    val selectedFrameBorder: BorderStroke? = null,
    val snapGuidesBorder: BorderStroke? = null,
    val alignmentGridBorder: BorderStroke? = null,
    val gridBorder: BorderStroke? = null,
    val snapToGrid: Boolean = true,
    val snapStepDp: Int = 20,
    val visibleStepDp: Int = 40,
    val rotationStepDegrees: Float = 4f,
    val scaleStepDp: Int = 4,
    /**
     * CENTER snapping zone, per axis, as a fraction of the container size.
     * Rendered as the center alignment grid; [UpCenterZone.all] sets both axes.
     */
    val centerZone: UpCenterZone = UpCenterZone.Default,
    val lockMove: Boolean = false,
    val lockRotation: Boolean = false,
    val lockScale: Boolean = false,
    /**
     * When true, a drag keeps the frame's current alignment and only the
     * paddings of that alignment are recomputed from the drop position, so
     * the frame can still move but never changes snaps. CENTER has no
     * paddings and therefore stays put; TOP/BOTTOM/START/END move along
     * their single free axis. Unlike [lockMove], the drag itself is allowed.
     */
    val lockSnaps: Boolean = false
)

@Deprecated(
    "Use UpEditSettings (renamed for scope clarity).",
    ReplaceWith("UpEditSettings", "com.mdsw.uplayout.UpEditSettings")
)
typealias UpGridSettings = UpEditSettings
