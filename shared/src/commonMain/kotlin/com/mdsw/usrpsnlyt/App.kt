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
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mdsw.uplayout.UpAlignment
import com.mdsw.uplayout.UpGridSettings
import com.mdsw.uplayout.UpItem
import com.mdsw.uplayout.UpLayout
import com.mdsw.uplayout.UpPadding
import com.mdsw.uplayout.UpScreenConfig
import com.mdsw.uplayout.rememberUpScreenConfigStore
import org.jetbrains.compose.resources.stringResource

import userpositionedlayout.shared.generated.resources.Res
import userpositionedlayout.shared.generated.resources.app_name
import userpositionedlayout.shared.generated.resources.edit_layout
import userpositionedlayout.shared.generated.resources.save_layout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var menuExpanded by remember { mutableStateOf(false) }
        var isEditMode by rememberSaveable { mutableStateOf(true) }
        var gridSettings by remember { mutableStateOf(UpGridSettings()) }
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
                                            if (isEditMode) Res.string.save_layout else Res.string.edit_layout
                                        )
                                    )
                                },
                                onClick = {
                                    isEditMode = !isEditMode
                                    menuExpanded = false
                                }
                            )
                            if (isEditMode) {
                                LockToggleItem(
                                    label = "Lock move",
                                    locked = gridSettings.lockMove,
                                    onToggle = {
                                        gridSettings = gridSettings.copy(lockMove = !gridSettings.lockMove)
                                    }
                                )
                                LockToggleItem(
                                    label = "Lock rotation",
                                    locked = gridSettings.lockRotation,
                                    onToggle = {
                                        gridSettings = gridSettings.copy(lockRotation = !gridSettings.lockRotation)
                                    }
                                )
                                LockToggleItem(
                                    label = "Lock scale",
                                    locked = gridSettings.lockScale,
                                    onToggle = {
                                        gridSettings = gridSettings.copy(lockScale = !gridSettings.lockScale)
                                    }
                                )
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
                            StepControl(
                                label = "Move",
                                value = "${gridSettings.snapGridSize}",
                                onMinus = {
                                    gridSettings = gridSettings.copy(
                                        snapGridSize = (gridSettings.snapGridSize - 1).coerceAtLeast(1)
                                    )
                                },
                                onPlus = {
                                    gridSettings = gridSettings.copy(
                                        snapGridSize = gridSettings.snapGridSize + 1
                                    )
                                }
                            )
                            StepControl(
                                label = "Rotate",
                                value = "${gridSettings.rotationStepDegrees}",
                                onMinus = {
                                    gridSettings = gridSettings.copy(
                                        rotationStepDegrees = (gridSettings.rotationStepDegrees - 1f).coerceAtLeast(1f)
                                    )
                                },
                                onPlus = {
                                    gridSettings = gridSettings.copy(
                                        rotationStepDegrees = gridSettings.rotationStepDegrees + 1f
                                    )
                                }
                            )
                            StepControl(
                                label = "Scale",
                                value = "${gridSettings.scaleStepDp}",
                                onMinus = {
                                    gridSettings = gridSettings.copy(
                                        scaleStepDp = (gridSettings.scaleStepDp - 1).coerceAtLeast(1)
                                    )
                                },
                                onPlus = {
                                    gridSettings = gridSettings.copy(
                                        scaleStepDp = gridSettings.scaleStepDp + 1
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            var config by remember {
                mutableStateOf(
                    UpScreenConfig(
                        frames = listOf(
                            UpItem(
                                id = "a",
                                alignment = UpAlignment.START_TOP,
                                padding = UpPadding(top = 16, start = 16),
                                widthDp = 240,
                                heightDp = 140
                            ),
                            UpItem(
                                id = "b",
                                alignment = UpAlignment.END_BOTTOM,
                                padding = UpPadding(bottom = 16, end = 16),
                                widthDp = 240,
                                heightDp = 140
                            )
                        )
                    )
                )
            }
            UpLayout(
                items = config.frames,
                onItemChanged = { updated ->
                    config = config.copy(
                        frames = config.frames.map { if (it.id == updated.id) updated else it }
                    )
                },
                isEditMode = isEditMode,
                grid = gridSettings,
                configStore = rememberUpScreenConfigStore(),
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                content = listOf(
                    {
                        Text(
                            text = "Drag me (a)",
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased()
                        )
                    },
                    {
                        Text(
                            text = "Drag me (b)",
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased()
                        )
                    }
                )
            )
        }
    }
}

@Composable
private fun LockToggleItem(
    label: String,
    locked: Boolean,
    onToggle: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(if (locked) "$label ✓" else label) },
        onClick = onToggle
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
