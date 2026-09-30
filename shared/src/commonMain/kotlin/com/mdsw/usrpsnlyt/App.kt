package com.mdsw.usrpsnlyt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mdsw.uplayout.UpAlignment
import com.mdsw.uplayout.UpEditSettings
import com.mdsw.uplayout.UpItem
import com.mdsw.uplayout.UpLayout
import com.mdsw.uplayout.UpPadding
import com.mdsw.uplayout.UpScreenConfig
import com.mdsw.uplayout.UpScreenConfigStore
import com.mdsw.uplayout.rememberUpScreenConfigStore
import org.jetbrains.compose.resources.stringResource

import userpositionedlayout.shared.generated.resources.Res
import userpositionedlayout.shared.generated.resources.app_name
import userpositionedlayout.shared.generated.resources.drag_me_a
import userpositionedlayout.shared.generated.resources.drag_me_b
import userpositionedlayout.shared.generated.resources.edit_layout
import userpositionedlayout.shared.generated.resources.lock_move
import userpositionedlayout.shared.generated.resources.lock_rotation
import userpositionedlayout.shared.generated.resources.lock_scale
import userpositionedlayout.shared.generated.resources.lock_snaps
import userpositionedlayout.shared.generated.resources.show_alignment_grid
import userpositionedlayout.shared.generated.resources.show_frame_bounds
import userpositionedlayout.shared.generated.resources.show_grid
import userpositionedlayout.shared.generated.resources.show_snap_guides
import userpositionedlayout.shared.generated.resources.snap_to_grid
import userpositionedlayout.shared.generated.resources.step_move
import userpositionedlayout.shared.generated.resources.step_rotate
import userpositionedlayout.shared.generated.resources.step_scale
import userpositionedlayout.shared.generated.resources.view_layout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(persistLayout: Boolean = true) {
    MaterialTheme {
        var menuExpanded by remember { mutableStateOf(false) }
        var isEditMode by rememberSaveable { mutableStateOf(true) }
        var editSettings by remember { mutableStateOf(UpEditSettings()) }
        val configStore: UpScreenConfigStore? =
            if (persistLayout) rememberUpScreenConfigStore() else null
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                    actions = {
                        IconButton(onClick = { menuExpanded = true }) {
                            OverflowIcon()
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            if (isEditMode) Res.string.view_layout else Res.string.edit_layout
                                        )
                                    )
                                },
                                onClick = {
                                    isEditMode = !isEditMode
                                    menuExpanded = false
                                }
                            )
                            if (isEditMode) {
                                val toggles = listOf(
                                    Triple(
                                        stringResource(Res.string.show_grid),
                                        editSettings.showGrid,
                                    ) { v: UpEditSettings -> v.copy(showGrid = !v.showGrid) },
                                    Triple(
                                        stringResource(Res.string.show_alignment_grid),
                                        editSettings.showAlignmentGrid,
                                    ) { v: UpEditSettings -> v.copy(showAlignmentGrid = !v.showAlignmentGrid) },
                                    Triple(
                                        stringResource(Res.string.show_snap_guides),
                                        editSettings.showSnapGuides,
                                    ) { v: UpEditSettings -> v.copy(showSnapGuides = !v.showSnapGuides) },
                                    Triple(
                                        stringResource(Res.string.show_frame_bounds),
                                        editSettings.showFrameBounds,
                                    ) { v: UpEditSettings -> v.copy(showFrameBounds = !v.showFrameBounds) },
                                    Triple(
                                        stringResource(Res.string.snap_to_grid),
                                        editSettings.snapToGrid,
                                    ) { v: UpEditSettings -> v.copy(snapToGrid = !v.snapToGrid) },
                                    Triple(
                                        stringResource(Res.string.lock_move),
                                        editSettings.lockMove,
                                    ) { v: UpEditSettings -> v.copy(lockMove = !v.lockMove) },
                                    Triple(
                                        stringResource(Res.string.lock_rotation),
                                        editSettings.lockRotation,
                                    ) { v: UpEditSettings -> v.copy(lockRotation = !v.lockRotation) },
                                    Triple(
                                        stringResource(Res.string.lock_scale),
                                        editSettings.lockScale,
                                    ) { v: UpEditSettings -> v.copy(lockScale = !v.lockScale) },
                                    Triple(
                                        stringResource(Res.string.lock_snaps),
                                        editSettings.lockSnaps,
                                    ) { v: UpEditSettings -> v.copy(lockSnaps = !v.lockSnaps) }
                                )
                                toggles.forEach { (label, checked, update) ->
                                    ToggleMenuItem(label, checked) {
                                        editSettings = update(editSettings)
                                    }
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (isEditMode) {
                    Surface {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val steps = listOf(
                                StepSpec(
                                    label = stringResource(Res.string.step_move),
                                    value = editSettings.snapStepDp.toString(),
                                    onMinus = {
                                        editSettings = editSettings.copy(
                                            snapStepDp = (editSettings.snapStepDp - 1).coerceAtLeast(1)
                                        )
                                    },
                                    onPlus = {
                                        editSettings = editSettings.copy(
                                            snapStepDp = editSettings.snapStepDp + 1
                                        )
                                    }
                                ),
                                StepSpec(
                                    label = stringResource(Res.string.step_rotate),
                                    value = editSettings.rotationStepDegrees.toString(),
                                    onMinus = {
                                        editSettings = editSettings.copy(
                                            rotationStepDegrees = (editSettings.rotationStepDegrees - 1f).coerceAtLeast(1f)
                                        )
                                    },
                                    onPlus = {
                                        editSettings = editSettings.copy(
                                            rotationStepDegrees = editSettings.rotationStepDegrees + 1f
                                        )
                                    }
                                ),
                                StepSpec(
                                    label = stringResource(Res.string.step_scale),
                                    value = editSettings.scaleStepDp.toString(),
                                    onMinus = {
                                        editSettings = editSettings.copy(
                                            scaleStepDp = (editSettings.scaleStepDp - 1).coerceAtLeast(1)
                                        )
                                    },
                                    onPlus = {
                                        editSettings = editSettings.copy(
                                            scaleStepDp = editSettings.scaleStepDp + 1
                                        )
                                    }
                                )
                            )
                            steps.forEach { spec ->
                                StepControl(spec.label, spec.value, spec.onMinus, spec.onPlus)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            var config by remember { mutableStateOf(demoConfig()) }
            val cardTexts = listOf(
                stringResource(Res.string.drag_me_a),
                stringResource(Res.string.drag_me_b)
            )
            UpLayout(
                items = config.items,
                onItemChanged = { updated ->
                    config = config.copy(
                        items = config.items.map { if (it.id == updated.id) updated else it }
                    )
                },
                isEditMode = isEditMode,
                settings = editSettings,
                configStore = configStore,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                content = cardTexts.map { text -> { DemoCard(text) } }
            )
        }
    }
}

@Composable
private fun ToggleMenuItem(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
        },
        onClick = onToggle
    )
}

private data class StepSpec(
    val label: String,
    val value: String,
    val onMinus: () -> Unit,
    val onPlus: () -> Unit
)

private fun demoConfig() = UpScreenConfig(
    items = listOf(
        UpItem("a", UpAlignment.START_TOP, UpPadding(top = 16, start = 16), 240, 140),
        UpItem("b", UpAlignment.END_BOTTOM, UpPadding(bottom = 16, end = 16), 240, 140)
    )
)

@Composable
private fun DemoCard(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.CenterVertically),
        textAlign = TextAlign.Center,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased()
    )
}

@Composable
private fun StepControl(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onMinus) { Text("-") }
            Text(value)
            TextButton(onClick = onPlus) { Text("+") }
        }
    }
}

@Composable
private fun OverflowIcon(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp)) {
        val radius = 2.dp.toPx()
        val centerX = size.width / 2f
        listOf(0.25f, 0.5f, 0.75f).forEach { fraction ->
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(centerX, size.height * fraction)
            )
        }
    }
}
