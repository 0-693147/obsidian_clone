@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.obsidianclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.R
import com.example.obsidianclone.SettingsViewModel


@Composable
fun SettingsScreen (
    view: SettingsViewModel,
    navController: NavController,
) {
    val fontSize by view.fontSize.collectAsStateWithLifecycle()
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
        bottomBar = {BottomBar(navController)}
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
                val rowModifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                item {
                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Note Font", color = Colors.textColor)
                        Box(modifier = Modifier.fillMaxWidth(0.7f)){
                            FontMenu(view)
                        }
                    }
                }
                item {
                    val min = 1
                    val max = 30
                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Note font Size", color = Colors.textColor)
                        Text(
                            text = fontSize.toString(),
                            color = Colors.textColor,
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentWidth(Alignment.End)
                        )
                    }
                    Slider(
                        modifier = Modifier.padding(8.dp),
                        value = fontSize.toFloat(),
                        steps = max - min - 1,
                        valueRange = min.toFloat()..max.toFloat(),
                        onValueChange = { view.setFontSize(it.toInt()) }
                    )
                }
                item {
                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
    navController: NavController,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Colors.backgroundColor)
            .padding(18.dp)
            .fillMaxWidth(),
    ) {
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item() {
                IconButton(
                    onClick = {
                        navController.popBackStack()
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
fun FontMenu(
    view: SettingsViewModel
) {
    val fonts = listOf("Default", "Monospace", "Serif", "Sans-serif")
    var expanded by remember { mutableStateOf(false)}
    val selectedFont by view.font.collectAsStateWithLifecycle()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedFont,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .width(IntrinsicSize.Min)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fonts.forEach { font ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = font,
                            fontFamily = when (font) {
                                "Monospace" -> FontFamily.Monospace
                                "Serif" -> FontFamily.Serif
                                "Sans-serif" -> FontFamily.SansSerif
                                else -> FontFamily.Default
                            }
                        )
                    },
                    onClick = {
                        view.setFont(font)
                        expanded = false
                    }
                )
            }
        }
    }
}
