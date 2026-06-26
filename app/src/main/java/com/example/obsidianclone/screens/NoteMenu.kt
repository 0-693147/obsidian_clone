package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.DirectoryScreenRoute
import com.example.obsidianclone.GraphScreenRoute
import com.example.obsidianclone.LightNote
import com.example.obsidianclone.NoteMenuViewModel
import com.example.obsidianclone.NoteRoute
import com.example.obsidianclone.R
import com.example.obsidianclone.SearchScreenRoute
import com.example.obsidianclone.SettingsScreenRoute

@Composable fun NoteMenu(
    navController: NavController,
    view: NoteMenuViewModel,
    directoryId: Int? = null,
) {
    LaunchedEffect(directoryId) {
        if (directoryId != null) view.setDirectoryId(directoryId)
        view.updateLightNoteList()
        view.setPath()
    }

    val notes by view.notes.collectAsStateWithLifecycle(emptyList())
    val path = view.thisPath

    Scaffold(
        topBar = {TopBar(path.value, navController)},
        bottomBar = {BottomBar(view, navController, notes, directoryId)},
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) { innerPadding ->
        if (notes.isEmpty()) {

            println("notes")
            println(notes)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = "No notes yet",
                        color = Colors.textColor.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create one",
                        color = Colors.textColor.copy(alpha = 0.3f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(color = Colors.backgroundColor)
            ) {
                NoteList(
                    navController = navController,
                    view = view,
                    noteList = notes,
                    directoryId = directoryId
                )
                Spacer(modifier = Modifier.fillMaxHeight())
            }
        }
    }
}

@Composable
private fun TopBar(
    path : String = "/path",
    navControler: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Colors.backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                onClick = {
                    navControler.navigate(DirectoryScreenRoute)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_folder_24  ),
                    contentDescription = "Folder Icon",
                    tint = Colors.textColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Text(text = path, color = Color.White)
        }
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = {
                    navControler.navigate(SettingsScreenRoute)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.settings_icon),
                    contentDescription = "Graph Icon",
                    tint = Colors.textColor
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    view: NoteMenuViewModel,
    navController: NavController,
    noteList: List<LightNote>,
    directoryId: Int?,
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
            item {
                IconButton(
                    onClick = {
                        navController.navigate(SearchScreenRoute)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = "Search Icon",
                        tint = Colors.textColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = {
                        var title = "New Note"
                        var titleCollision = true
                        var i = 0
                        while (titleCollision) {
                            i++
                            titleCollision = false
                            noteList.forEach { note ->
                                println(note.title)
                                println(title)
                                println(1)
                                if (note.title == title) {
                                    titleCollision = true
                                    title = "New Note ($i)"
                                }
                            }
                        }
                        if (directoryId != null) view.createEmptyNote(title, directoryId)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_note_icon),
                        contentDescription = "Add Note Icon",
                        tint = Colors.textColor
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
                        tint = Colors.textColor
                    )
                }
            }
        }
    }
}

@Composable
fun NoteList(
    navController: NavController,
    view: NoteMenuViewModel,
    noteList: List<LightNote>,
    directoryId: Int?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(36.dp)
    ) {
        items(noteList) { note ->
            var isContextVisible = remember { mutableStateOf(false) }
            Column() {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = Colors.panelColor)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .pointerInput(true) {
                            detectTapGestures(
                                onLongPress = {
                                    isContextVisible.value = true
                                },
                                onTap = {
                                    println(note)
                                    navController.navigate(route = NoteRoute(note.id))
                                }
                            )
                        }
                ) {
                    Column() {
                        Text(text = note.title, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (note.snippet.isNotBlank()) {
                            Text(
                                text = note.snippet.trim(),
                                color = Colors.textColor.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                ContextMenu(
                    isContextVisible = isContextVisible,
                    deleteNoteFunction = { view.deleteNote(note.id) },
                    addNewNoteFunction = { if (directoryId != null) view.createEmptyNote(directoryId = directoryId) },
                    title = note.title
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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
        onDismissRequest = { isContextVisible.value = false},
    ) {
        DropdownMenuItem(
            text = { Text(text = "Delete " + title) },
            onClick = {
                deleteNoteFunction()
                isContextVisible.value = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Add new note") },
            onClick = {
                addNewNoteFunction()
                isContextVisible.value = false
            }
        )
    }
}
