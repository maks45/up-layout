package com.mdsw.uplayout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun UpLayout(
    items: List<UpItem>,
    onItemChanged: (UpItem) -> Unit,
    modifier: Modifier = Modifier,
    grid: UpGridSettings = UpGridSettings(),
    alignmentCenter: Dp = 20.dp,
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
        if (saved.frames.isNotEmpty()) {
            saved.frames.forEach(onItemChanged)
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
            grid = grid,
            alignmentCenter = alignmentCenter,
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
                modifier = Modifier
                    .align(item.alignment.toComposeAlignment())
                    .upItemBounds(item)
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
    grid: UpGridSettings = UpGridSettings(),
    alignmentCenter: Dp = 20.dp,
    onItemClick: (UpItem) -> Unit = {},
    content: List<@Composable BoxScope.() -> Unit>
) {
    val density = LocalDensity.current
    val containerSize = remember { mutableStateOf(IntSize.Zero) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val primary = MaterialTheme.colorScheme.primary
    val childSizes = remember { mutableStateMapOf<String, IntSize>() }
    val latestItems by rememberUpdatedState(items)
    val latestOnItemChanged by rememberUpdatedState(onItemChanged)
    val latestGrid by rememberUpdatedState(grid)

    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .dashedBorder(MaterialTheme.colorScheme.primary)
            .drawAlignmentGrid(MaterialTheme.colorScheme.primary, alignmentCenter)
            .drawSnapGrid(
                dotSpacing = grid.visibleGridSize.dp,
                showGrid = grid.showGrid
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
                                lockedMode = resolveTransformLock(accumZoom, accumRotation)
                            }
                            val mode = lockedMode
                            val currentSelectedId = selectedId
                            if (mode != null && currentSelectedId != null) {
                                val current =
                                    latestItems.firstOrNull { it.id == currentSelectedId }
                                if (current != null) {
                                    when (mode) {
                                        UpTransformMode.SCALE -> {
                                            if (zoom != 1f) {
                                                val sizePx =
                                                    childSizes[current.id] ?: IntSize.Zero
                                                val densityVal = density.density
                                                val fallbackW = if (sizePx.width > 0) {
                                                    sizePx.width / densityVal -
                                                        (current.padding.start + current.padding.end)
                                                } else {
                                                    current.widthDp?.toFloat() ?: 100f
                                                }
                                                val fallbackH = if (sizePx.height > 0) {
                                                    sizePx.height / densityVal -
                                                        (current.padding.top + current.padding.bottom)
                                                } else {
                                                    current.heightDp?.toFloat() ?: 100f
                                                }
                                                val (newW, newH) = resolveScaledSize(
                                                    currentWidthDp = current.widthDp,
                                                    currentHeightDp = current.heightDp,
                                                    fallbackWidthDp = fallbackW,
                                                    fallbackHeightDp = fallbackH,
                                                    zoom = zoom,
                                                    stepDp = latestGrid.scaleStepDp
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
                                                            stepDegrees = latestGrid.rotationStepDegrees
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
        items.forEachIndexed { index, item ->
            val dragOffset = remember { mutableStateOf(IntOffset.Zero) }
            val childRect = remember { mutableStateOf(Rect.Zero) }
            val latestItem by rememberUpdatedState(item)
            val isSelected = selectedId == item.id
            key(item.id) {
                Box(
                    modifier = Modifier
                        .align(item.alignment.toComposeAlignment())
                        .upItemBounds(item)
                        .then(
                            if (isSelected) Modifier.border(2.dp, primary)
                            else Modifier.dashedItemBorder(Color.Gray)
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
                                    val size = containerSize.value
                                    if (size.width > 0 && size.height > 0) {
                                        val pxToDp = 1f / density.density
                                        val rect = childRect.value
                                        val result = resolveDrop(
                                            containerWidthDp = size.width * pxToDp,
                                            containerHeightDp = size.height * pxToDp,
                                            childLeftDp = rect.left * pxToDp,
                                            childTopDp = rect.top * pxToDp,
                                            childRightDp = rect.right * pxToDp,
                                            childBottomDp = rect.bottom * pxToDp,
                                            alignmentCenterDp = alignmentCenter.value,
                                            snapStepDp = latestGrid.snapGridSize,
                                            snapToGrid = latestGrid.snapToGrid
                                        )
                                        val current = latestItem
                                        latestOnItemChanged(
                                            current.copy(
                                                alignment = result.alignment,
                                                padding = result.padding
                                            )
                                        )
                                    }
                                    dragOffset.value = IntOffset.Zero
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                dragOffset.value += IntOffset(
                                    x = dragAmount.x.toInt(),
                                    y = dragAmount.y.toInt()
                                )
                            }
                        }
                        .onGloballyPositioned {
                            childRect.value = it.boundsInParent()
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

private fun Modifier.upItemBounds(item: UpItem): Modifier {
    return this
        .padding(
            top = item.padding.top.dp,
            bottom = item.padding.bottom.dp,
            start = item.padding.start.dp,
            end = item.padding.end.dp
        )
        .then(if (item.widthDp != null) Modifier.width(item.widthDp.dp) else Modifier)
        .then(if (item.heightDp != null) Modifier.height(item.heightDp.dp) else Modifier)
        .rotate(item.rotationDegrees)
}

private fun UpAlignment.toComposeAlignment(): Alignment {
    return when (this) {
        UpAlignment.CENTER -> Alignment.Center
        UpAlignment.TOP -> Alignment.TopCenter
        UpAlignment.BOTTOM -> Alignment.BottomCenter
        UpAlignment.START -> Alignment.CenterStart
        UpAlignment.END -> Alignment.CenterEnd
        UpAlignment.START_TOP -> Alignment.TopStart
        UpAlignment.END_TOP -> Alignment.TopEnd
        UpAlignment.START_BOTTOM -> Alignment.BottomStart
        UpAlignment.END_BOTTOM -> Alignment.BottomEnd
    }
}

private fun Modifier.dashedBorder(color: Color) = this.drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    )
}

private fun Modifier.dashedItemBorder(color: Color, width: Dp = 1.dp) = this.drawBehind {
    drawRect(
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    )
}

private fun Modifier.drawAlignmentGrid(color: Color, centerSize: Dp) =
    this.drawBehind {
        val horizontalDistance = size.width / 2f - centerSize.toPx() / 2
        val verticalDistance = size.height / 2f - centerSize.toPx() / 2

        drawLine(
            color = color,
            start = Offset(horizontalDistance, 0f),
            end = Offset(horizontalDistance, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = color,
            start = Offset(size.width - horizontalDistance, 0f),
            end = Offset(size.width - horizontalDistance, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = color,
            start = Offset(0f, verticalDistance),
            end = Offset(size.width, verticalDistance),
            strokeWidth = 1f
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height - verticalDistance),
            end = Offset(size.width, size.height - verticalDistance),
            strokeWidth = 1f
        )
    }

private fun Modifier.drawSnapGrid(
    dotSpacing: Dp,
    showGrid: Boolean,
    color: Color = Color.Gray
): Modifier {
    if (!showGrid) return this
    return this.drawBehind {
        val spacingPx = dotSpacing.toPx()
        if (spacingPx <= 0f) return@drawBehind
        val xCount = (size.width / spacingPx).toInt()
        val yCount = (size.height / spacingPx).toInt()
        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                drawCircle(
                    color = color,
                    radius = 1f,
                    center = Offset(x * spacingPx, y * spacingPx)
                )
            }
        }
    }
}
