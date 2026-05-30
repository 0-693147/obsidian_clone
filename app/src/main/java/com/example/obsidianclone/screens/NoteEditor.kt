package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.NoteScreenRoute
import com.example.obsidianclone.NoteViewModel
import com.example.obsidianclone.R


//@Preview
//@Composable
//private fun Preview() {
//    NoteEditor ( navConvroller = rememberNavController() )
//}

@Composable
fun NoteEditor(
    navController: NavController,
    view: NoteViewModel,
    id: Int
) {
    view.retrieveFullNote(id)
    val thisNote by view.selectedNote.collectAsStateWithLifecycle()
    var textState = remember { mutableStateOf(thisNote?.text?: "") }
    var titleState = remember { mutableStateOf(thisNote?.title?: "") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars)
        ,
        topBar = {NoteTitleBar(id, titleState, view)},
        bottomBar = {BottomBar(navController)}
    ) {innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(color = Colors.backgroundColor)
                .fillMaxSize()
        ) {
            TextField(
                modifier = Modifier.background(color = Colors.backgroundColor),
                textStyle = TextStyle(
                    fontSize = 4.em
                ),
                value = textState.value,
                onValueChange = {textValue ->
                    textState.value = textValue
                    view.updateNoteText(id, textValue)
                },
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
    }
}


@Composable
private fun NoteTitleBar (
    id: Int,
    titleState: MutableState<String>,
    view: NoteViewModel
) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Colors.backgroundColor)
    ) {
        Column() {
            Text(text = "id: " + id.toString())
            TextField(
                modifier = Modifier
                    .background(color = Colors.backgroundColor),
                placeholder = { "Your Title" },
                value = titleState.value,
                onValueChange = {newTitle ->
                    titleState.value = newTitle
                    view.updateNoteTitle(id, newTitle)
                },
                textStyle = TextStyle(
                    fontSize = 10.em
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Colors.backgroundColor,
                    unfocusedContainerColor = Colors.backgroundColor,
                    unfocusedTextColor = Colors.textColor,
                    focusedTextColor = Colors.textColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }
}

@Composable
private fun BottomBar(navController: NavController) {
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
                    onClick = { navController.navigate(NoteScreenRoute)}
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
