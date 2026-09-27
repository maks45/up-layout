package com.mdsw.usrpsnlyt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mdsw.uplayout.UpAlignment
import com.mdsw.uplayout.UpItem
import com.mdsw.uplayout.UpPadding
import com.mdsw.uplayout.UserPositionedLayout
import org.jetbrains.compose.resources.stringResource

import userpositionedlayout.shared.generated.resources.Res
import userpositionedlayout.shared.generated.resources.app_name
import userpositionedlayout.shared.generated.resources.edit_layout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var menuExpanded by remember { mutableStateOf(false) }
        var isEditMode by remember { mutableStateOf(true) }
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
                                text = { Text(stringResource(Res.string.edit_layout)) },
                                onClick = {
                                    isEditMode = !isEditMode
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            var items by remember {
                mutableStateOf(
                    listOf(
                        UpItem(
                            id = "a",
                            alignment = UpAlignment.START_TOP,
                            padding = UpPadding(top = 16, start = 16)
                        ),
                        UpItem(
                            id = "b",
                            alignment = UpAlignment.END_BOTTOM,
                            padding = UpPadding(bottom = 16, end = 16)
                        )
                    )
                )
            }
            UserPositionedLayout(
                items = items,
                onItemChanged = { updated ->
                    items = items.map { if (it.id == updated.id) updated else it }
                },
                isEditMode = isEditMode,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) { item ->
                Text(
                    text = "Drag me (${item.id})",
                    modifier = Modifier.padding(24.dp)
                )
            }
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
