package com.example.obsidianclone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

var textColor = Color.White
var backgroundColor = Color.hsv(0f, 0f, .1f)

var panelColor = Color.hsv(0f, 0f, .2f)

@Preview(showBackground = true)
@Composable
fun NotesScreen() {
    Scaffold(
        topBar = {Path("/path")},
        bottomBar = {BottomBar()}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(color = backgroundColor)
        ) {
            NoteList()
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
            .background(color = backgroundColor)
            .padding(8.dp)
    ) {
        Text(text = path, color = Color.White)
    }
}

@Composable
fun BottomBar() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = backgroundColor)
            .padding(36.dp)
            .fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item() {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = "Search Icon",
                        tint = textColor
                    )
                }
            }
            item() {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_note_icon),
                        contentDescription = "Add Note Icon",
                        tint = textColor
                    )
                }
            }
            item() {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.graph_icon),
                        contentDescription = "Graph Icon",
                        tint = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun NoteList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(36.dp)
    ) {
        items(20) { index ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = panelColor)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Note $index", color = Color.White)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
