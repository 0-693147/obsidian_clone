@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.obsidianclone.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.obsidianclone.Colors
import com.example.obsidianclone.LightNote
import com.example.obsidianclone.NoteAsset
import com.example.obsidianclone.NoteEditViewModel
import com.example.obsidianclone.NoteMenuRoute
import com.example.obsidianclone.R
import com.example.obsidianclone.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteEditor(
    navController: NavController,
    view: NoteEditViewModel,
    viewSettings: SettingsViewModel,
    thisNoteId: Int
) {
    LaunchedEffect(thisNoteId) {
        view.retrieveFullNote(thisNoteId);
        view.retrieveNoteAssets(thisNoteId)
    }

    val thisNote by view.selectedNote.collectAsStateWithLifecycle()
    val thisNoteLoaded by view.selectedNoteLoaded.collectAsStateWithLifecycle()
    val assetTypeIndices = mapOf("all" to 0, "image" to 1, "audio" to 2, "video" to 3)
    val thisNoteAssets by view.noteAssets.collectAsStateWithLifecycle()
    val showChooseAssetTypeDialog = remember { mutableStateOf(false) }

    val images = thisNoteAssets.filter{ asset ->
        asset.contentType == assetTypeIndices["image"]
    }
    val audios = thisNoteAssets.filter{ asset ->
        asset.contentType == assetTypeIndices["audio"]
    }
    val videos = thisNoteAssets.filter{ asset ->
        asset.contentType == assetTypeIndices["video"]
    }

    if (thisNoteLoaded == false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Colors.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val initialText = remember(thisNote) {
            thisNote?.text ?: ""
        }
        val initialTitle = remember(thisNote) {
            thisNote?.title ?: ""
        }
        var textState = remember { mutableStateOf(TextFieldValue(initialText)) }
        var titleState = remember { mutableStateOf(initialTitle) }
        val context = LocalContext.current

        println(context.filesDir)
        println(context.getExternalFilesDir(null))
        val rootDir = context.getExternalFilesDir(null)
        val imagesDir = File(rootDir, "images")
        imagesDir.mkdirs()
        println(imagesDir)


        @Composable
        fun addAssetButton(assetType: String, onDismissRequest: () -> Unit) {
            val launcher = rememberLauncherForActivityResult(
                contract =
                    ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                println("uri")
                println(uri)
                uri?.let {
                    val ts = LocalDateTime.now()
                    val fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnnnnn")
                    val filename = ts.format(fmt) + "_" + assetType
                    val file = File(imagesDir, filename)
                    val content = context.contentResolver.openInputStream(uri)?.use {
                        it.buffered().readBytes()
                    }
                    file.outputStream().use { out ->
                        out.write(content)
                    }
                    view.createNoteAsset(
                        noteId = thisNoteId,
                        type = assetTypeIndices.getValue(assetType),
                        link = file.toUri()
                    )
                }
            }
            TextButton(
                modifier = Modifier
                    .background(color = Color.Transparent),
                onClick = {
                    println("asset adding start")
                    launcher.launch(assetType + "/*")
                    println("asset adding end")
//                    onDismissRequest()
                }
            ) {
                Text(assetType)
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Colors.backgroundColor)
                .windowInsetsPadding(WindowInsets.systemBars),
            topBar = { NoteTitleBar(thisNoteId, titleState, view) },
            bottomBar = { BottomBar(
                view,
                navController,
                showChooseAssetTypeDialog
            ) }
        ) { innerPadding ->
            Box() {

                if (showChooseAssetTypeDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showChooseAssetTypeDialog.value = false },
                        containerColor = Colors.panelColor,
                        title = { Text("Add attachment", color = Colors.textColor) },
                        text = {
                            Column {
                                addAssetButton("image") { showChooseAssetTypeDialog.value = false }
                                addAssetButton("audio") { showChooseAssetTypeDialog.value = false }
                                addAssetButton("video") { showChooseAssetTypeDialog.value = false }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showChooseAssetTypeDialog.value = false }) {
                                Text("Cancel", color = Colors.highlightTextColor)
                            }
                        }
                    )
                }



                val current: TextFieldValue = textState.value
                val cursor: Int = current.selection.start  // where the cursor is in the string
                val text: String = current.text            // the full string


                if (view.isNoteLinkPickerVisible.value) NoteLinkPicker(
                    view = view,
                    onDismiss = { view.isNoteLinkPickerVisible.value = false},
                    onNoteSelected = { note ->
                        val link = "[[${note.title}]]"
                        val current = textState.value
                        val cursor = current.selection.start
                        val newText =
                            current.text.substring(0, cursor) + link + current.text.substring(
                                cursor
                            )
                        textState.value = TextFieldValue(
                            text = newText,
                            selection = TextRange(cursor + link.length)
                        )
                        view.updateNoteText(thisNoteId, newText)
                        view.isNoteLinkPickerVisible.value = false
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .background(color = Colors.backgroundColor)
                        .fillMaxSize()
                ) {
                    item() {
                        Spacer(
                            modifier = Modifier
                                .height(8.dp)
                        )
                    }

                    items(images) { image ->
                        AssetItem(onDelete = { view.deleteNoteAsset(image.id, thisNoteId) }) {
                            AsyncImage(
                                model = image.link,
                                contentDescription = "image",
                                modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }

                    items(videos) { video ->
                        AssetItem(onDelete = { view.deleteNoteAsset(video.id, thisNoteId) }) {
                            VideoAsset(video, context)
                        }
                    }

                    items(audios) { audio ->
                        AssetItem(onDelete = { view.deleteNoteAsset(audio.id, thisNoteId) }) {
                            AudioAsset(audio, context)
                        }
                    }

                    val linkRegex = Regex("""\[\[([^\]]+)]]""")
                    var linkMatches: List<String>



                    item() {
                        val font by viewSettings.font.collectAsStateWithLifecycle()
                        val fontFamily = when (font) {
                            "Monospace" -> FontFamily.Monospace
                            "Serif" -> FontFamily.Serif
                            "Sans-serif" -> FontFamily.SansSerif
                            else -> FontFamily.Default
                        }
                        val fontSize by viewSettings.fontSize.collectAsStateWithLifecycle()

                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 200.dp),
                            textStyle = TextStyle(
                                fontSize = fontSize.sp,
                                fontFamily = fontFamily
                            ),
                            value = textState.value,
                            onValueChange = { textValue ->
                                val links = linkRegex.findAll(textValue.text)
                                    .map { match ->
                                        println(match)
                                        val (link) = match.destructured
                                        return@map link
                                    }
                                    .toList()
                                view.handleLinks(links)
                                textState.value = textValue
                                view.updateNoteText(thisNoteId, textValue.text)
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Colors.backgroundColor,
                                unfocusedContainerColor = Colors.backgroundColor,
//                                unfocusedTextColor = Colors.textColor,
//                                focusedTextColor = Colors.textColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(1000.dp))
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AssetItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val isDeleteVisible = remember { mutableStateOf(false) }
    Column {
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        isDeleteVisible.value = true
                    })
                }
        ) {
            content()
        }
        ContextMenu(
            isContextVisible = isDeleteVisible,
            deleteImageCallback = onDelete,
            text = "Delete"
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoAsset(video: NoteAsset, context: Context) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.link))
            prepare()
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp)),
        factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer } }
    )
}

//@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@androidx.annotation.OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioAsset(audio: NoteAsset, context: Context) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audio.link))
            prepare()
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }

    // update progress every frame while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            progress = if (exoPlayer.duration > 0)
                exoPlayer.currentPosition.toFloat() / exoPlayer.duration
            else 0f
            duration = exoPlayer.duration
            delay(200)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (isPlaying) {
                exoPlayer.pause()
                isPlaying = false
            } else {
                exoPlayer.play()
                isPlaying = true
            }
        }) {
            Icon(
                painter = painterResource(
                if (isPlaying) R.drawable.play_circle_24dp_e3e3e3_fill0_wght400_grad0_opsz24
                    else R.drawable.pause_circle_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
                ),
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Slider(
            value = progress,
            thumb = {
                Box(
                    Modifier
                        .size(20.dp)
//                        .padding(4.dp)
                        .background(Color.White, CircleShape)
                )
            },
            track = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(5.dp)
                            .background(Color.White)
                    )
                }
            },
            onValueChange = { newProgress ->
                exoPlayer.seekTo((newProgress * exoPlayer.duration).toLong())
                progress = newProgress
            },
//                MaterialTheme.colorScheme.background,
            modifier = Modifier
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.background)
        )

        Text(
            text = formatDuration(duration - (duration * progress).toLong()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Composable
private fun NoteTitleBar (
    id: Int,
    titleState: MutableState<String>,
    view: NoteEditViewModel
) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Colors.backgroundColor)
    ) {
        Column() {
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
                    fontSize = 7.em
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
private fun BottomBar(
    view: NoteEditViewModel,
    navController: NavController,
    isChooseAssetTypeDialogVisible: MutableState<Boolean>,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Colors.backgroundColor)
            .padding(18.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
        }
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                IconButton(
                    onClick = { view.isNoteLinkPickerVisible.value = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.add_link_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                        contentDescription = "Insert link",
                        tint = Colors.textColor
                    )
                }
            }
            item() {
                IconButton(
                    onClick = {
                        val directoryId = view.selectedNote.value?.directoryId
                        if (directoryId != null) { navController.navigate(NoteMenuRoute(directoryId = directoryId))
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
            item() {
                IconButton(
                    modifier = Modifier
                        .zIndex(1f)
                    ,
                    onClick = {
                        isChooseAssetTypeDialogVisible.value = true
                    }
                ) {
                    Icon (
                        painter = painterResource(R.drawable.attach_file_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                        contentDescription = "Attachment Icon",
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
    deleteImageCallback: () -> Unit,
    text: String,
) {
    DropdownMenu(
        expanded = isContextVisible.value,
        onDismissRequest = { isContextVisible.value = false},
    ) {
        DropdownMenuItem(
            text = { Text(text = text) },
            onClick = {
                deleteImageCallback()
                isContextVisible.value = false
            }
        )
    }
}


@Composable
fun NoteLinkPicker(
    view: NoteEditViewModel,
    onDismiss: () -> Unit,
    onNoteSelected: (LightNote) -> Unit
) {
    view.retrieveNoteList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert link", color = Colors.textColor) },
        containerColor = Colors.panelColor,
        text = {
            LazyColumn {
                items(view.notes.value) { note ->
                    Text(
                        text = note.title,
                        color = Colors.textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNoteSelected(note) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Colors.highlightTextColor)
            }
        }
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MissingPermissionsComponent(
    content: @Composable () -> Unit,
    permissions: List<String>,
    permissionText: String,
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = permissions
    )

    if (permissionsState.allPermissionsGranted) {
        content()
    } else {
        Button(
            onClick = {
                println("invoking permission dialogue")
                permissionsState.launchMultiplePermissionRequest()
            }
        ) {
            Text(text = permissionText?: "Request permissions")
        }
    }
}

