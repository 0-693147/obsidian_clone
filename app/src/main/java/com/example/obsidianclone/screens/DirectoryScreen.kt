package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.DirectoryNode
import com.example.obsidianclone.DirectoryScreenViewModel
import com.example.obsidianclone.GraphScreenRoute
import com.example.obsidianclone.NoteMenuRoute
import com.example.obsidianclone.R
import com.example.obsidianclone.SearchScreenRoute


@Composable fun DirectoryScreen(
    view: DirectoryScreenViewModel,
    navController: NavController
) {
    var tree = view.tree.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            DirectoryTopBar(
                path = "/",
                navController = navController
            )
        },
        bottomBar = {
            DirectoryBottomBar(
                navController = navController,
                addFolderFunction = {
                    if (tree != null) {
                        view.createDirectory(
                            name = "New Directory",
                            parentId = tree.directory.id
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.backgroundColor)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            if (tree != null) {
                val flattened = tree!!.flatten()
                    .drop(1)
                directoryList(
                    navController = navController,
                    view = view,
                    directoriesFlattened = flattened
                )
            }
            if (view.isRenameCardVisible) RenameCard(view)
        }
    }
}


@Composable
private fun directoryList(
    navController: NavController,
    view: DirectoryScreenViewModel,
    directoriesFlattened: List<Pair<Int, DirectoryNode>>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.backgroundColor)
            .padding(16.dp)
    ) {
        items(directoriesFlattened) { (depth, directory) ->
            val isContextVisible = remember { mutableStateOf(false) }
            val isHighlighted = remember { mutableStateOf(false) }
            Column {
                DirectoryRow(
                    navController = navController,
                    directory = directory,
                    depth = depth,
                    isHighlighted = isHighlighted,
                    isContextVisible = isContextVisible
                )
                Box(modifier = Modifier.padding(start = (depth * 20).dp)) {
                    DirectoryDropDownMenu(
                        view = view,
                        directory = directory,
                        isContextVisible = isContextVisible,
                        isHighlighted = isHighlighted,
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectoryRow(
    navController: NavController,
    directory: DirectoryNode,
    depth: Int,
    isHighlighted: MutableState<Boolean>,
    isContextVisible: MutableState<Boolean>,
) {
    val panelColor = if (isHighlighted.value) Colors.panelColorFocused else Colors.panelColor
    Box(
        modifier = Modifier
            .padding(start = (depth * 20).dp)
            .clip(RoundedCornerShape(4.dp))
            .background(panelColor)
            .fillMaxWidth()
            .height(30.dp)
            .pointerInput(directory.directory.hasOnlyNotes) {
                detectTapGestures(
                    onLongPress = {
                        isContextVisible.value = true
                        isHighlighted.value = true
                    },
                    onTap = {
                        println("tapped")
                        println("has only notes:")
                        println(directory.directory.hasOnlyNotes)
                        if (directory.directory.hasOnlyNotes) {
                            navController.navigate(NoteMenuRoute(directory.directory.id))
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(
                    painter = painterResource(
                        if (directory.directory.hasOnlyNotes) {
                            R.drawable.outline_folder_24_green
                        } else {
                            R.drawable.outline_folder_24
                        }
                    ),
                    contentDescription = "Folder Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = directory.directory.name,
                color = Color.White
            )
        }
    }
}


@Composable
private fun DirectoryTopBar(
    path: String,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Colors.backgroundColor)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = path,
            color = Colors.textColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
@Composable
private fun DirectoryBottomBar(
    navController: NavController,
    addFolderFunction: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(Colors.backgroundColor)
            .padding(36.dp)
            .fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            item {
                IconButton(
                    onClick = {
                        navController.navigate(SearchScreenRoute)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = "Search Icon",
                        tint = Colors.textColor,
                    )
                }
            }

            item {
                IconButton(
                    onClick = {
                        addFolderFunction()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_create_new_folder_24),
                        contentDescription = "Add Folder Icon",
                        tint = Colors.textColor,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            item {
                IconButton(
                    onClick = {
                        navController.navigate(GraphScreenRoute)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.graph_icon),
                        contentDescription = "Graph Icon",
                        tint = Colors.textColor,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DirectoryDropDownMenu(
    view: DirectoryScreenViewModel,
    directory: DirectoryNode,
    isContextVisible: MutableState<Boolean>,
    isHighlighted: MutableState<Boolean>,
) {
    DropdownMenu(
        expanded = isContextVisible.value,
        onDismissRequest = {
            isContextVisible.value = false
            isHighlighted.value = false
        },
    ) {
        DropdownMenuItem(
            text = { Text(text = "Delete " + directory.directory.name) },
            onClick = {
                isHighlighted.value = false
                view.deleteDirectory(directory.directory.id)
                isContextVisible.value = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Rename " + directory.directory.name) },
            onClick = {
                isHighlighted.value = false
                view.isRenameCardVisible = true
                view.focusedDirectory = directory
                isContextVisible.value = false
            }
        )
        if (!directory.directory.hasOnlyNotes) DropdownMenuItem(
            text = { Text("Add new directory") },
            onClick = {
                isHighlighted.value = false
                view.createDirectory("New Directory", directory.directory.id)
                isContextVisible.value = false
            }
        )

        if (directory.children.isEmpty()) DropdownMenuItem(
            text = {
                Text(if (directory.directory.hasOnlyNotes) "Make it a folder" else "Make it a note directory")
            },
            onClick = {
                isHighlighted.value = false
                view.updateDirectoryType(directory.directory.id, !directory.directory.hasOnlyNotes)
                isContextVisible.value = false
            }
        )
    }
}


@Composable
fun RenameCard(
    view: DirectoryScreenViewModel
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = {
            view.isRenameCardVisible= false
        }
    ) {
        Card(
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text("Rename")
            }
            var textState = remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .background(color = Colors.panelColor)
                        .defaultMinSize(minHeight = 50.dp),
                    textStyle = TextStyle(
                        fontSize = 4.em
                    ),
                    placeholder = { Text(text = view.focusedDirectory?.directory?.name ?: "") },
                    value = textState.value,
                    onValueChange = { textValue ->
                        textState.value = textValue
                    },
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Colors.backgroundColor,
                        unfocusedContainerColor = Colors.backgroundColor,
                        unfocusedTextColor = Colors.textColor,
                        focusedTextColor = Colors.textColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                TextButton(
                    onClick = {
                        view.isRenameCardVisible = false
                    }
                ) {
                    Text("Dismiss")
                }
                TextButton(
                    onClick = {
                        view.focusedDirectory?.directory?.id?.let { id ->
                            view.renameDirectory(id, textState.value.trim())
                        }
                        view.isRenameCardVisible = false
                    }
                ) {
                    Text("Apply")
                }
            }
        }
    }
}