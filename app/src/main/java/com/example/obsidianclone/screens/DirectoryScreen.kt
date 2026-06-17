package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.GraphScreenRoute
import com.example.obsidianclone.NoteMenuRoute
import com.example.obsidianclone.R
import com.example.obsidianclone.SearchScreenRoute

data class DirectoryItem(
    val name: String,
    val level: Int,
    val hasOnlyNotes: Boolean
)

@Composable
fun DirectoryScreen(
    navController: NavController
) {
    val directories = remember {
        mutableStateListOf(
            DirectoryItem("Uni", 0, false),
            DirectoryItem("ML", 1, false),
            DirectoryItem("Pentesting", 1, false),
            DirectoryItem("OWASP", 2, true),
            DirectoryItem("Cooking", 0, true),
            DirectoryItem("Overanalysing", 0, false),
            DirectoryItem("exes", 1, true)
        )
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
                    directories.add(
                        DirectoryItem(
                            name = "New Folder",
                            level = 0,
                            hasOnlyNotes = false
                        )
                    )
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.backgroundColor)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Colors.backgroundColor)
                .padding(
                    start = 36.dp,
                    end = 36.dp,
                    top = 32.dp
                )
        ) {
            items(directories) { directory ->
                DirectoryRow(directory)
                Spacer(modifier = Modifier.height(14.dp))
            }
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
private fun DirectoryRow(
    directory: DirectoryItem
) {
    Box(
        modifier = Modifier
            .padding(start = (directory.level * 44).dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Colors.panelColor)
            .fillMaxWidth()
            .height(30.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = directory.name,
                color = Color.White
            )
        }
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
                        modifier = Modifier.size(56.dp)
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