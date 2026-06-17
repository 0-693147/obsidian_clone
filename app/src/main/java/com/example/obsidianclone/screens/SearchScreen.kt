package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.NoteMenuRoute
import com.example.obsidianclone.NoteMenuViewModel
import com.example.obsidianclone.NoteMenuViewModel.NoteSearchResult
import com.example.obsidianclone.NoteRoute
import com.example.obsidianclone.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    view: NoteMenuViewModel,
    navController: NavController
) {
    val searchResults by view.searchResults.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars),
        bottomBar = {BottomBar(navController)}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(color = Colors.backgroundColor)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            var textFieldState by remember { mutableStateOf("")}
            var expanded by rememberSaveable { mutableStateOf(false) }

            val onActiveChange = {}
            val colors1 = SearchBarDefaults.colors(
                containerColor = Colors.backgroundColor,
                dividerColor = Color.Transparent,
                inputFieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Colors.panelColor,
                    unfocusedContainerColor = Colors.panelColor,
                )
            )
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = textFieldState,
                        onQueryChange = { newText ->
                            textFieldState = newText
                            view.launchSearchWithDelay(query = textFieldState)
                        },
                        onSearch = {},
                        expanded = true,
                        onExpandedChange = {},
                        enabled = true,
                        placeholder = { Text("Search notes", color = Colors.textColor) },
                        leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.search_icon),
                                    contentDescription = "Search Icon",
                                    tint = Colors.textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                        trailingIcon = {},
                        colors = colors1.inputFieldColors,
                        interactionSource = null,
                    )
                },
                expanded = true,
                onExpandedChange = {},
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .semantics { traversalIndex = 0f },
                shape = SearchBarDefaults.inputFieldShape,
                colors = colors1,
                tonalElevation = 0.dp,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets,
                content = {
                    SearchResults(navController, searchResults)
                },
            )
        }
    }
}

@Composable
private fun SearchResults(
    navController: NavController,
    searchResults: List<NoteSearchResult>
) {
    LazyColumn() {
        println(searchResults)
        if (searchResults.isNullOrEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matches")
                }
            }
        }
        items(searchResults) { result ->
            val note = result.ligthNote
            LazyColumn(
                modifier = Modifier
                    .heightIn(0.dp, 10000.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Colors.panelColor)
                    .clickable(
                        onClick = {
                            navController.navigate( route = NoteRoute(note.id))
                        }
                    )
            ) {
                item {
                    Box(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            note.title,
                            fontSize = 6.em
                        )
                    }
                }
                items(result.matches) { match ->
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                    val annotatedString = buildAnnotatedString {
                        val prefix = "..."
                        val postfix = "..."
                        append(prefix + match.snippet.trim() + postfix)
                        match.highlightRange.forEach { range ->
                            addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Colors.highlightTextColor
                                ),
                                start = range.first + prefix.length,
                                end = range.last + prefix.length + 1
                            )
                        }
                    }
                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = annotatedString
                    )
                }
            }
        }
    }
}



@Composable
private fun BottomBar(
    navController: NavController
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
                        navController.navigate(NoteMenuRoute) {
                            popUpTo(NoteMenuRoute)
                        }
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
