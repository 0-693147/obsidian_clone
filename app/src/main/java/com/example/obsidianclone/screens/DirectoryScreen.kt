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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.DirectoryItem
import com.example.obsidianclone.DirectoryScreenViewModel
import com.example.obsidianclone.GraphScreenRoute
import com.example.obsidianclone.NoteMenuRoute
import com.example.obsidianclone.R
import com.example.obsidianclone.SearchScreenRoute


@Composable fun DirectoryScreen(
    view: DirectoryScreenViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    view.root = remember {
        view.loadDirectoryStructure(context) ?: DirectoryItem(
            name = "root",
            level = 0,
            hasOnlyNotes = false,
            parent = null,
            children = mutableStateListOf()
        ).also { root ->
            val allNotes = DirectoryItem(
                name = "All Notes",
                level = 1,
                hasOnlyNotes = false,
                parent = root,
                children = mutableStateListOf()
            )
            val myNotes = DirectoryItem(
                name = "My Notes",
                level = 2,
                hasOnlyNotes = true,
                parent = allNotes,
                children = mutableStateListOf()
            )
            allNotes.children.add(myNotes)
            root.children.add(allNotes)
        }
    }

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
                    view.root?.children?.add(
                        DirectoryItem(
                            name = "New Folder",
                            level = 1,
                            hasOnlyNotes = false,
                            parent = view.root,
                            children = mutableStateListOf()
                        )
                    )
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
            val root = view.root
            if (root != null) directoryList(
                view,
                directoriesFlattened = root.flatten().slice(1..<root.flatten().size),
                )
            if (view.isRenameCardVisible) RenameCard(view)
        }
    }
}


@Composable
private fun directoryList(
    view: DirectoryScreenViewModel,
    directoriesFlattened: List<DirectoryItem>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.backgroundColor)
            .padding(16.dp)
    ) {
        items(directoriesFlattened) { directory ->
            val isContextVisible = remember { mutableStateOf(false) }
            val isHighlighted = remember { mutableStateOf(false) }
            Column() {
                DirectoryRow(
                    directory,
                    isHighlighted,
                    isContextVisible
                )
                Box(modifier = Modifier.padding(start = (directory.level * 20).dp)) {
                    DirectoryDropDownMenu(
                        view,
                        directory,
                        isContextVisible,
                        isHighlighted,
                        directory.hasOnlyNotes
                    )
                }
            }
        }
    }
}


@Composable
private fun DirectoryRow(
    directory: DirectoryItem,
    isHighlighted: MutableState<Boolean>,
    isContextVisible: MutableState<Boolean>,
) {
    val panelColor = if (isHighlighted.value) Colors.panelColorFocused else Colors.panelColor
    Box(
        modifier = Modifier
            .padding(start = (directory.level * 20).dp)
            .clip(RoundedCornerShape(4.dp))
            .background(panelColor)
            .fillMaxWidth()
            .height(30.dp)
            .pointerInput(true) {
            detectTapGestures(
                onLongPress = {
                    isContextVisible.value = true
                    isHighlighted.value = true
                },
                onTap = {
//                    navController.navigate( route = NoteRoute(note.id))
                }
            )
        },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(
                    painter = painterResource(
                        if (directory.hasOnlyNotes) {
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
                text = directory.name,
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
        IconButton(
            onClick = {
                navController.navigate(NoteMenuRoute)
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = 14.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.return_icon),
                contentDescription = "Back Icon",
                tint = Colors.textColor
            )
        }
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
    directory: DirectoryItem,
    isContextVisible: MutableState<Boolean>,
    isHighlighted: MutableState<Boolean>,
    isNotesOnly: Boolean,
) {
    val context = LocalContext.current
    DropdownMenu(
        expanded = isContextVisible.value,
        onDismissRequest = {
            isContextVisible.value = false
            isHighlighted.value = false
           },
    ) {
        DropdownMenuItem(
            text = { Text(text = "Delete " + directory.name) },
            onClick = {
                directory.delete()
                view.saveDirectoryStructure(context)
                isContextVisible.value = false
            }
        )
        if (!isNotesOnly) DropdownMenuItem(
            text = { Text(text = "Add new directory") },
            onClick = {
                directory.addSubdirectory(
                    DirectoryItem(
                        name = "New Directory",
                        level = directory.level + 1,
                        hasOnlyNotes = false,
                        parent = directory,
                        children = mutableStateListOf()
                    ),
                )
                view.saveDirectoryStructure(context)
                isContextVisible.value = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Rename " + directory.name) },
            onClick = {
                view.isRenameCardVisible = true
                isContextVisible.value = false
                view.focusedDirectory = directory
            }
        )
        if (!isNotesOnly and directory.children.isEmpty()) DropdownMenuItem(
            text = { Text(text = "Make it a note directory") },
            onClick = {
                directory.hasOnlyNotes = true
                view.saveDirectoryStructure(context)
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
                    placeholder = { Text(text = view.focusedDirectory?.name ?: "") },
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
                        view.focusedDirectory?.rename(textState.value.trim())
                        view.saveDirectoryStructure(context)
                        view.isRenameCardVisible = false
                    }
                ) {
                    Text("Apply")
                }
            }
        }
    }
}