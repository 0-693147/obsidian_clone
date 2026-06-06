package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.obsidianclone.Colors
import com.example.obsidianclone.R


@Preview
@Composable
fun SettingsScreen () {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Colors.backgroundColor)
                    .padding(16.dp)
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Settings",
                    color = Colors.textColor,
                    fontSize = 7.em
                )
            }
        },
        bottomBar = {BottomBar()}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(color = Colors.backgroundColor)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                val rowModifier = Modifier.padding(16.dp)
                item {
                    Row(
                        modifier = rowModifier,
                    ) {
                        Text(text = "Font", color = Colors.textColor)
                    }
                }
                item {
                    val min = 8
                    val max = 16
                    val default = 12
                    var sliderPosition by remember {
                        mutableFloatStateOf(default.toFloat() ) }
                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Font Size", color = Colors.textColor)
                        Text(
                            text = sliderPosition.toInt().toString(),
                            color = Colors.textColor,
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentWidth(Alignment.End)
                        )
                    }
                    Slider(
                        modifier = Modifier.padding(8.dp),
                        value = sliderPosition,
                        steps = max - min - 1,
                        valueRange = min.toFloat()..max.toFloat(),
                        onValueChange = { sliderPosition = it }
                    )
                }
                item {
                    Row(
                        modifier = rowModifier
                    ) {
                        Text(text = "Language", color = Colors.textColor)
                    }
                }
            }
        }
    }
}


@Composable
private fun BottomBar(
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Colors.backgroundColor)
            .padding(36.dp)
            .fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item() {
                IconButton(
                    onClick = {
//                        navController.navigate(NoteMenuRoute) {
//                            popUpTo(NoteMenuRoute)
//                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.return_icon),
                        contentDescription = "Search Icon",
                        tint = Colors.textColor
                    )
                }
            }
        }
    }
}


@Composable
private fun ContextMenu(
    isContextVisible: MutableState<Boolean>,
    deleteNoteFunction: () -> Unit,
    addNewNoteFunction: () -> Unit,
    title: String
) {
    DropdownMenu(
        expanded = isContextVisible.value,
        onDismissRequest = { isContextVisible.value = false },
    ) {
        DropdownMenuItem(
            text = { Text(text = "Delete " + title) },
            onClick = {
                deleteNoteFunction()
                isContextVisible.value = false
            }
        )
    }
}
