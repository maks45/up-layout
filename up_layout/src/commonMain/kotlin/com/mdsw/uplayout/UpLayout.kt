package com.mdsw.uplayout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * User-manipulable container. In edit mode children can be dragged, pinched to
 * scale and twisted to rotate; in view mode items are placed read-only.
 * Persist via [configStore]; pass null to disable. [content] parallels [items].
 */
@Composable
fun UpLayout(
    items: List<UpItem>,
    onItemChanged: (UpItem) -> Unit,
    modifier: Modifier = Modifier,
    settings: UpEditSettings = UpEditSettings(),
    centerDeadZone: Dp = 20.dp,
    isEditMode: Boolean = true,
    onItemClick: (UpItem) -> Unit = {},
    configStore: UpScreenConfigStore? = null,
    content: List<@Composable BoxScope.() -> Unit>
) {
    require(items.size == content.size) {
        "UpLayout: items and content must have the same size, got ${items.size} items and ${content.size} contents"
    }
    var restoreDone by remember(configStore) { mutableStateOf(configStore == null) }
    LaunchedEffect(configStore) {
        val store = configStore ?: return@LaunchedEffect
        val saved = store.load()
        if (saved.items.isNotEmpty()) {
            saved.items.forEach(onItemChanged)
        }
        restoreDone = true
    }
    LaunchedEffect(items, restoreDone) {
        if (restoreDone) {
            configStore?.save(UpScreenConfig(items))
        }
    }
    if (isEditMode) {
        EditModeLayout(
            items = items,
            onItemChanged = onItemChanged,
            modifier = modifier,
            settings = settings,
            centerDeadZone = centerDeadZone,
            onItemClick = onItemClick,
            content = content
        )
    } else {
        ViewModeLayout(
            items = items,
            modifier = modifier,
            content = content
        )
    }
}

@Composable
private fun ViewModeLayout(
    items: List<UpItem>,
    modifier: Modifier = Modifier,
    content: List<@Composable BoxScope.() -> Unit>
) {
    Box(modifier = modifier) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = upPlacedItem(item)
            ) {
                content[index]()
            }
        }
    }
}

@Composable
private fun EditModeLayout(
    items: List<UpItem>,
    onItemChanged: (UpItem) -> Unit,
    modifier: Modifier = Modifier,
    settings: UpEditSettings = UpEditSettings(),
    centerDeadZone: Dp = 20.dp,
    onItemClick: (UpItem) -> Unit = {},
    content: List<@Composable BoxScope.() -> Unit>
) {
    val density = LocalDensity.current
    val containerSize = remember { mutableStateOf(IntSize.Zero) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val primary = MaterialTheme.colorScheme.primary
    val childSizes = remember { mutableStateMapOf<String, IntSize>() }
    val childRects = remember { mutableStateMapOf<String, Rect>() }
    val latestItems by rememberUpdatedState(items)
    val latestOnItemChanged by rememberUpdatedState(onItemChanged)
    val latestSettings by rememberUpdatedState(settings)
    val latestSelectedId by rememberUpdatedState(selectedId)
    val latestDensityValue by rememberUpdatedState(density.density)

    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .dashedBorder(MaterialTheme.colorScheme.primary)
            .drawAlignmentGrid(
                MaterialTheme.colorScheme.primary,
                centerDeadZone,
                settings.showAlignmentGrid,
                settings.alignmentGridBorder
            )
            .drawSnapGrid(
                dotSpacing = settings.visibleStepDp.dp,
                showGrid = settings.showGrid,
                border = settings.gridBorder
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { selectedId = null })
            }
            .pointerInput(Unit) {
                // Container-level multitouch: both fingers must reach the same handler.
                // Per-item handlers miss the gesture when the second finger lands
                // outside the item bounds, so scale/rotate the selected item here.
                // Single-finger events are NOT consumed to keep drag/tap working.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var lockedMode: UpTransformMode? = null
                    var accumZoom = 1f
                    var accumRotation = 0f
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.count { it.pressed } >= 2) {
                            val zoom = event.calculateZoom()
                            val rotationDelta = event.calculateRotation()
                            if (lockedMode == null) {
                                accumZoom *= zoom
                                accumRotation += rotationDelta
                                lockedMode = resolveTransformLock(
                                    accumZoom,
                                    accumRotation,
                                    scaleLocked = latestSettings.lockScale,
                                    rotationLocked = latestSettings.lockRotation
                                )
                            }
                            val mode = lockedMode
                            val currentSelectedId = latestSelectedId
                            if (mode != null && currentSelectedId != null) {
                                val current =
                                    latestItems.firstOrNull { it.id == currentSelectedId }
                                if (current != null) {
                                    when (mode) {
                                        UpTransformMode.SCALE -> {
                                            if (zoom != 1f) {
                                                val sizePx =
                                                    childSizes[current.id] ?: IntSize.Zero
                                                val densityVal = latestDensityValue
                                                val (fallbackW, fallbackH) = fallbackItemSizeDp(
                                                    sizePx = sizePx,
                                                    padding = current.padding,
                                                    widthDp = current.widthDp,
                                                    heightDp = current.heightDp,
                                                    density = densityVal
                                                )
                                                val (newW, newH) = resolveScaledSize(
                                                    currentWidthDp = current.widthDp,
                                                    currentHeightDp = current.heightDp,
                                                    fallbackWidthDp = fallbackW,
                                                    fallbackHeightDp = fallbackH,
                                                    zoom = zoom,
                                                    stepDp = latestSettings.scaleStepDp
                                                )
                                                latestOnItemChanged(
                                                    current.copy(widthDp = newW, heightDp = newH)
                                                )
                                            }
                                        }
                                        UpTransformMode.ROTATE -> {
                                            if (rotationDelta != 0f) {
                                                latestOnItemChanged(
                                                    current.copy(
                                                        rotationDegrees = resolveRotationDegrees(
                                                            currentDegrees = current.rotationDegrees,
                                                            deltaDegrees = rotationDelta,
                                                            stepDegrees = latestSettings.rotationStepDegrees
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .onGloballyPositioned { containerSize.value = it.size }
    ) {
        if (settings.showSnapGuides) {
            SnapGuidesOverlay(
                items = items,
                childRects = childRects,
                color = primary,
                border = settings.snapGuidesBorder
            )
        }
        items.forEachIndexed { index, item ->
            val dragOffset = remember { mutableStateOf(IntOffset.Zero) }
            val latestItem by rememberUpdatedState(item)
            val isSelected = selectedId == item.id
            key(item.id) {
                Box(
                    modifier = upPlacedItem(item)
                        .then(
                            if (!settings.showFrameBounds) Modifier
                            else if (isSelected) {
                                settings.selectedFrameBorder?.let { Modifier.border(it) }
                                    ?: Modifier.border(2.dp, primary)
                            } else {
                                settings.frameBorder?.let { Modifier.dashedBorderStroke(it) }
                                    ?: Modifier.dashedItemBorder(Color.Gray)
                            }
                        )
                        .offset { dragOffset.value }
                        .pointerInput(Unit) {
                            // Select on press (no consume) so a direct two-finger
                            // pinch selects on first down before container scales.
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                selectedId = latestItem.id
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    selectedId = latestItem.id
                                },
                                onDragEnd = {
                                    if (!latestSettings.lockMove) {
                                        val size = containerSize.value
                                        if (size.width > 0 && size.height > 0) {
                                            val pxToDp = 1f / latestDensityValue
                                            val rect = childRects[latestItem.id] ?: Rect.Zero
                                            val result = resolveDrop(
                                                containerWidthDp = size.width * pxToDp,
                                                containerHeightDp = size.height * pxToDp,
                                                childLeftDp = rect.left * pxToDp,
                                                childTopDp = rect.top * pxToDp,
                                                childRightDp = rect.right * pxToDp,
                                                childBottomDp = rect.bottom * pxToDp,
                                                centerDeadZoneDp = centerDeadZone.value,
                                                snapStepDp = latestSettings.snapStepDp,
                                                snapToGrid = latestSettings.snapToGrid
                                            )
                                            val current = latestItem
                                            latestOnItemChanged(
                                                current.copy(
                                                    alignment = result.alignment,
                                                    padding = result.padding
                                                )
                                            )
                                        }
                                    }
                                    dragOffset.value = IntOffset.Zero
                                }
                            ) { change, dragAmount ->
                                if (!latestSettings.lockMove) {
                                    change.consume()
                                    dragOffset.value += IntOffset(
                                        x = dragAmount.x.toInt(),
                                        y = dragAmount.y.toInt()
                                    )
                                }
                            }
                        }
                        .onGloballyPositioned {
                            val bounds = it.boundsInParent()
                            childRects[item.id] = bounds
                            childSizes[item.id] = it.size
                        }
                ) {
                    content[index]()
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                selectedId = item.id
                                onItemClick(item)
                            }
                    )
                }
            }
        }
    }
}
