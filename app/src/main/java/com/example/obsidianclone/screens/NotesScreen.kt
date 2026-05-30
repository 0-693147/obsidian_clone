package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.NoteRoute
import com.example.obsidianclone.NoteViewModel
import com.example.obsidianclone.Notes
import com.example.obsidianclone.R


//@Preview
//@Composable
//private fun Preview() {
//    val view = ViewModelProvider(this, factory)[NoteViewModel::class.java]
//    NotesScreen(
//        navController = rememberNavController(),
//        viewModel = view
//    )
//}
@Composable fun NotesScreen(
    navController: NavController,
    view: NoteViewModel,
) {
    val noteListState by view.notes.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {Path("/path")},
        bottomBar = {BottomBar(view)},
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(color = Colors.backgroundColor)
        ) {
            NoteList(
                navController = navController,
                viewModel = view,
                noteList = noteListState
            )
            Spacer(modifier = Modifier.fillMaxHeight())
        }
    }
}

@Composable
fun Path(path : String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Colors.backgroundColor)
            .padding(8.dp)
    ) {
        Text(text = path, color = Color.White)
    }
}

@Composable
private fun BottomBar(view: NoteViewModel) {
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
                    onClick = {}
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
                        view.createEmptyNote()
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
                    onClick = {}
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
    viewModel: NoteViewModel,
    noteList: List<Notes>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(36.dp)
    ) {
        items(noteList) { note ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = Colors.panelColor)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { navController.navigate( route = NoteRoute(note.id))}
                ) {
                    Text(text = note.title, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
