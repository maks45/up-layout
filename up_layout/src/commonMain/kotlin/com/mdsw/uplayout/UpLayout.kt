package com.mdsw.uplayout

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

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
    onItemClick: (UpItem) -> Unit = {},
    content: List<@Composable BoxScope.() -> Unit>
) {
    val density = LocalDensity.current.density
    val containerSize = remember { mutableStateOf(IntSize.Zero) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.colorScheme
    val childSizes = remember { mutableStateMapOf<String, IntSize>() }
    val childRects = remember { mutableStateMapOf<String, Rect>() }
    val latest by rememberUpdatedState(
        UpEditSnapshot(items, onItemChanged, settings, selectedId, density)
    )

    Box(
        modifier = modifier
            .upEditContainer(settings, colors.background, colors.primary)
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
                                    scaleLocked = latest.settings.lockScale,
                                    rotationLocked = latest.settings.lockRotation
                                )
                            }
                            latest.selectedId?.let { id ->
                                latest.items.firstOrNull { it.id == id }?.let { current ->
                                    when (lockedMode) {
                                        UpTransformMode.SCALE -> if (zoom != 1f) {
                                            latest.onItemChanged(
                                                scaledItem(
                                                    current,
                                                    childSizes[current.id] ?: IntSize.Zero,
                                                    latest.density,
                                                    zoom,
                                                    latest.settings.scaleStepDp
                                                )
                                            )
                                        }
                                        UpTransformMode.ROTATE -> if (rotationDelta != 0f) {
                                            latest.onItemChanged(
                                                current.copy(
                                                    rotationDegrees = resolveRotationDegrees(
                                                        current.rotationDegrees,
                                                        rotationDelta,
                                                        latest.settings.rotationStepDegrees
                                                    )
                                                )
                                            )
                                        }
                                        null -> Unit
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
                color = colors.primary,
                border = settings.snapGuidesBorder
            )
        }
        items.forEachIndexed { index, item ->
            val dragOffset = remember { mutableStateOf(IntOffset.Zero) }
            val latestItem by rememberUpdatedState(item)
            key(item.id) {
                Box(
                    modifier = upPlacedItem(item)
                        .upFrameBorder(selectedId == item.id, settings, colors.primary)
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
                                    dropUpdate(
                                        containerSize = containerSize.value,
                                        rect = childRects[latestItem.id] ?: Rect.Zero,
                                        item = latestItem,
                                        settings = latest.settings,
                                        density = latest.density
                                    )?.let(latest.onItemChanged)
                                    dragOffset.value = IntOffset.Zero
                                }
                            ) { change, dragAmount ->
                                if (!latest.settings.lockMove) {
                                    change.consume()
                                    dragOffset.value += IntOffset(
                                        dragAmount.x.toInt(),
                                        dragAmount.y.toInt()
                                    )
                                }
                            }
                        }
                        .onGloballyPositioned {
                            childRects[item.id] = it.boundsInParent()
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

private data class UpEditSnapshot(
    val items: List<UpItem>,
    val onItemChanged: (UpItem) -> Unit,
    val settings: UpEditSettings,
    val selectedId: String?,
    val density: Float
)

private fun scaledItem(
    current: UpItem,
    sizePx: IntSize,
    density: Float,
    zoom: Float,
    stepDp: Int
): UpItem {
    val (fallbackW, fallbackH) = fallbackItemSizeDp(sizePx, current.padding, current.widthDp, current.heightDp, density)
    val (newW, newH) = resolveScaledSize(current.widthDp, current.heightDp, fallbackW, fallbackH, zoom, stepDp = stepDp)
    return current.copy(widthDp = newW, heightDp = newH)
}

private fun dropUpdate(
    containerSize: IntSize,
    rect: Rect,
    item: UpItem,
    settings: UpEditSettings,
    density: Float
): UpItem? {
    if (settings.lockMove || containerSize.width <= 0 || containerSize.height <= 0) return null
    val pxToDp = 1f / density
    val result = resolveDrop(
        containerWidthDp = containerSize.width * pxToDp,
        containerHeightDp = containerSize.height * pxToDp,
        childLeftDp = rect.left * pxToDp,
        childTopDp = rect.top * pxToDp,
        childRightDp = rect.right * pxToDp,
        childBottomDp = rect.bottom * pxToDp,
        centerZone = settings.centerZone,
        snapStepDp = settings.snapStepDp,
        snapToGrid = settings.snapToGrid,
        lockedAlignment = if (settings.lockSnaps) item.alignment else null
    )
    return item.copy(alignment = result.alignment, padding = result.padding)
}
