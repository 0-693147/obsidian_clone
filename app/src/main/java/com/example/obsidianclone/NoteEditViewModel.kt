package com.example.obsidianclone

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class NoteEditModelFactory(
    private val repository: Repository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteEditViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")  }}

class NoteEditViewModel(
    private val repository: Repository
): ViewModel() {

    var notes = mutableStateOf<List<LightNote>>(emptyList())
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNoteLoaded = MutableStateFlow(false)
    val _noteAssets = MutableStateFlow<List<NoteAsset>>(emptyList<NoteAsset>())
    val noteAssets = _noteAssets.asStateFlow()

//    var isNoteLinkPickerVisible by mutableStateOf(false)
//        private set
//
//    fun showNoteLinkPicker() { isNoteLinkPickerVisible = true }
//    fun hideNoteLinkPicker() { isNoteLinkPickerVisible = false }


    var isNoteLinkPickerVisible = mutableStateOf(false)

    val selectedNote: StateFlow<Note?> = _selectedNote
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    fun retrieveFullNote(id: Int) {
        viewModelScope.launch {
            selectedNoteLoaded.value = false
            val note = repository.retrieveFullNote(id).getOrNull()
            _selectedNote.value = note
            selectedNoteLoaded.value = true
        }
    }

    private var savingNoteTextJob: Job? = null
    private var savingNoteTitleJob: Job? = null

    fun updateNoteText(id: Int, text: String) {
        savingNoteTextJob?.cancel()
        savingNoteTextJob = viewModelScope.launch {
            delay(200)
            repository.updateNoteText(id, text)
        }
    }

    fun updateNoteTitle(id: Int, title: String) {
        savingNoteTitleJob?.cancel()
        savingNoteTitleJob = viewModelScope.launch {
            delay(200)
            repository.updateNoteTitle(id, title)
        }
    }


    fun retrieveAssetsByType(id: Int, type: Int) {
        viewModelScope.launch {
            repository.retrieveNoteAssetsByType(id, type)
        }
    }

    fun retrieveNoteAssets(id: Int) {
        viewModelScope.launch {
            println("note assets retrieve start")
            val allAssets = repository.retrieveNoteAssets(id)
            _noteAssets.value = allAssets.getOrNull() ?: emptyList()
            println("note assets retrieved")
        }
    }

    fun createNoteAsset(noteId: Int, type: Int, link: Uri) {
        viewModelScope.launch {
            val asset = NoteAsset(
                noteId = noteId,
                contentType = type,
                link = link,
            )
            println("create note asset view")
            println(asset)
            repository.createNoteAsset(asset)
            retrieveNoteAssets(noteId)
        }
    }

    fun deleteNoteAsset(assetId: Int, noteId: Int) {
        viewModelScope.launch {
            repository.deleteNoteAsset(assetId)
            retrieveNoteAssets(noteId)
        }
    }

    private var updatingLinksJob: Job? = null


    fun retrieveNoteList() {
        val directoryId = selectedNote.value?.directoryId
        if (directoryId != null) {
            viewModelScope.launch {
                val noteList = repository.retrieveNoteListLight().getOrNull()
                println("all notes: $noteList")
                notes.value = noteList?.filter { it.directoryId == directoryId } ?: emptyList()
            }
        }
    }

    fun handleLinks(noteLinkList: List<String>) {
        updatingLinksJob?.cancel()
        updatingLinksJob = viewModelScope.launch {
            delay(2000)
            val notes: List<LightNote>? = repository.retrieveNoteListLight().getOrNull()
            if (notes.isNullOrEmpty()) return@launch
            val thisNoteId = _selectedNote.value?.id
            if (thisNoteId == null) return@launch
            val titleNotes = notes.associateBy { it.title }
            val idNotes = notes.associateBy { it.id }
            val thisNoteExistingConnections =
                repository.retrieveConnectionsByNode(thisNoteId).getOrNull() ?: return@launch
//            create if there is a new connection
            noteLinkList.forEach { otherNoteTitle ->
                println("other note title")
                println(otherNoteTitle)
                println("title notes")
                println(titleNotes)
                val note = titleNotes[otherNoteTitle]
                println("note")
                println(note)
                if (note != null) {
                    val thisNoteId = _selectedNote.value?.id
                    val otherNoteId = note.id
                    thisNoteId?.let { thisNoteId ->
                        repository.createNodeConnection(thisNoteId, otherNoteId)
                    }
                }
            }
//            delete if there is no such connection no more
            thisNoteExistingConnections.forEach { nodeConnection ->
                val otherNoteTitle = idNotes[nodeConnection.otherNoteId]?.title
                if (noteLinkList.indexOf(otherNoteTitle) == -1) {
                    repository.deleteNodeConnection(nodeConnection.thisNoteId, nodeConnection.otherNoteId)
                }
            }
        }
    }
}
