package com.example.obsidianclone.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.obsidianclone.Colors
import com.example.obsidianclone.NoteEditViewModel
import com.example.obsidianclone.NoteMenuRoute
import com.example.obsidianclone.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


//@Preview
//@Composable
//private fun Preview() {
//    NoteEditor ( navConvroller = rememberNavController() )
//}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteEditor(
    navController: NavController,
    view: NoteEditViewModel,
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
    println("Images:")
    println(images)
    println("Audios:")
    println(images)
    println("Videos:")
    println(images)

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
        var textState = remember { mutableStateOf(initialText) }
        var titleState = remember { mutableStateOf(initialTitle) }
        val context = LocalContext.current

        println(context.filesDir)
        println(context.getExternalFilesDir(null))
        val rootDir = context.getExternalFilesDir(null)
        val imagesDir = File(rootDir, "images")
        imagesDir.mkdirs()
        println(imagesDir)

//        val imagePickerLauncher =

        @Composable
        fun addAssetButton(assetType: String, onDismissRequest: () -> Unit) {
            val launcher = rememberLauncherForActivityResult(
                contract =
                    ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
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
                    launcher.launch(assetType + "/*")
                    onDismissRequest()
                }
            ) {
                Text(assetType)
            }
        }

        val imagePickerLauncher = rememberLauncherForActivityResult(contract =
            ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val ts = LocalDateTime.now()
                val fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnnnnn")
                val filename = ts.format(fmt)
                println(ts.format(fmt))
                val file = File(imagesDir, filename)
                val content = context.contentResolver.openInputStream(uri)?.use {
                    it.buffered().readBytes()
                }
                file.outputStream().use { out ->
                    out.write(content)
                }
                view.createNoteAsset(
                    noteId = thisNoteId,
                    type = assetTypeIndices.getValue("image"),
                    link = file.toUri()
                )
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Colors.backgroundColor)
                .windowInsetsPadding(WindowInsets.systemBars),
            topBar = { NoteTitleBar(thisNoteId, titleState, view) },
            bottomBar = { BottomBar(
                navController,
                imagePickerLauncher,
                showChooseAssetTypeDialog
            ) }
        ) { innerPadding ->
            Box() {

                if (showChooseAssetTypeDialog.value) {
                    Dialog(
                        onDismissRequest = { showChooseAssetTypeDialog.value = false },
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    text = "Choose file type",
                                    textAlign = TextAlign.Center,
                                )
                                HorizontalDivider()
                                addAssetButton(
                                    "image",
                                    { showChooseAssetTypeDialog.value = false }
                                )
                                addAssetButton(
                                    "audio",
                                    { showChooseAssetTypeDialog.value = false }
                                )
                                addAssetButton(
                                    "video",
                                    { showChooseAssetTypeDialog.value = false }
                                )
                                Button(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    onClick = { showChooseAssetTypeDialog.value = false }
                                ) {
                                    Text(text = "Dismiss")
                                }
                            }
                        }
                    }
                }

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
                        val isDeleteAssetContextMenuVisible = remember { mutableStateOf(false) }
                        MissingPermissionsComponent(
                            permissions = listOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                            ),
                            permissionText = "To view images, grant storage permissions",
                            content = {
                                AsyncImage(
                                    model = image.link,
                                    contentDescription = "image",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .pointerInput(Unit) {
                                            detectTapGestures(onLongPress = {
                                                isDeleteAssetContextMenuVisible.value = true
                                            }
                                            )
                                        }
                                )
                                ContextMenu(
                                    isContextVisible = isDeleteAssetContextMenuVisible,
                                    deleteImageCallback = {
                                        view.deleteNoteAsset(image.id, thisNoteId)
                                    },
                                    text = "Delete Image"
                                )
                            },
                        )
                    }

                    items(videos) { video ->
                        val isDeleteAssetContextMenuVisible = remember { mutableStateOf(false) }
                        MissingPermissionsComponent(
                            permissions = listOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                            ),
                            permissionText = "To view images, grant storage permissions",
                            content = {
                                Column(
                                ) {
                                    val exoPlayer = ExoPlayer.Builder(context).build()
                                    val mediaItem = MediaItem.fromUri(video.link)
                                    exoPlayer.setMediaItem(mediaItem)
                                    exoPlayer.prepare()

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .pointerInput(Unit) {
                                                detectTapGestures(onLongPress = {
                                                    isDeleteAssetContextMenuVisible.value = true
                                                }
                                                )
                                            }
                                    ) {
                                        AndroidView(

                                            factory = { ctx ->
                                                PlayerView(
                                                    ctx,
                                                ).apply {
                                                    player = exoPlayer
                                                    setOnLongClickListener {
                                                        isDeleteAssetContextMenuVisible.value = true
                                                        true
                                                    }
                                                }
                                            },
                                        )
                                    }

                                    ContextMenu(
                                        isContextVisible = isDeleteAssetContextMenuVisible,
                                        deleteImageCallback = {
                                            view.deleteNoteAsset(video.id, thisNoteId)
                                        },
                                        text = "Delete Video"
                                    )
                                }
                            },
                        )
                    }

                    items(audios) { audio ->
                        val isDeleteAssetContextMenuVisible = remember { mutableStateOf(false) }
                        MissingPermissionsComponent(
                            permissions = listOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                            ),
                            permissionText = "To view images, grant storage permissions",
                            content = {
                                Column(
                                ) {
                                    val exoPlayer = ExoPlayer.Builder(context).build()
                                    val mediaItem = MediaItem.fromUri(audio.link)
                                    exoPlayer.setMediaItem(mediaItem)
                                    exoPlayer.prepare()

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .pointerInput(Unit) {
                                                detectTapGestures(onLongPress = {
                                                    isDeleteAssetContextMenuVisible.value = true
                                                }
                                                )
                                            }
                                    ) {
                                        AndroidView(

                                            factory = { ctx ->
                                                PlayerView(
                                                    ctx,
                                                ).apply {
                                                    player = exoPlayer
                                                    setOnLongClickListener {
                                                        isDeleteAssetContextMenuVisible.value = true
                                                        true
                                                    }
                                                }
                                            },
                                        )
                                    }

                                    ContextMenu(
                                        isContextVisible = isDeleteAssetContextMenuVisible,
                                        deleteImageCallback = {
                                            view.deleteNoteAsset(audio.id, thisNoteId)
                                        },
                                        text = "Delete Audio"
                                    )
                                }
                            },
                        )
                    }
                    val linkRegex = Regex("""\[\[([^\]]+)]]""")
                    var linkMatches: List<String>
                    item() {
                        TextField(
                            modifier = Modifier
                                .background(color = Colors.backgroundColor)
                                .defaultMinSize(minHeight = 200.dp)
                            ,
                            textStyle = TextStyle(
                                fontSize = 4.em
                            ),
                            value = textState.value,
                            onValueChange = { textValue ->
                                val links = linkRegex.findAll(textValue)
                                    .map { match ->
                                        println(match)
                                        val (link) = match.destructured
                                        return@map link
                                    }
                                    .toList()
                                view.handleLinks(links)
                                textState.value = textValue
                                view.updateNoteText(thisNoteId, textValue)
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
        }
    }
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
    navController: NavController,
    launcher: ActivityResultLauncher<String>,
    isChooseAssetTypeDialogVisible: MutableState<Boolean>
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Colors.backgroundColor)
            .padding(36.dp)
            .fillMaxWidth(),
    ) {
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item() {
                IconButton(
                    onClick = { navController.navigate(NoteMenuRoute) {
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
            item() {
                IconButton(
                    modifier = Modifier
                        .zIndex(1f)
                    ,
                    onClick = {
//                        launcher.launch("image/*")
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



@OptIn(ExperimentalPermissionsApi::class) // 1.
@Composable
fun MissingPermissionsComponent(
    content: @Composable () -> Unit, // 2.
    permissions: List<String>,
    permissionText: String,
) {
    val permissionsState = rememberMultiplePermissionsState( // 5.
        permissions = permissions
    )

    if (permissionsState.allPermissionsGranted) { // 6.
        content()
    } else {
        Button(
            onClick = {
                println("invoking permission dialogue")
                permissionsState.launchMultiplePermissionRequest() // 7.
            }
        ) {
            Text(text = permissionText?: "Request permissions")
        }
    }
}

