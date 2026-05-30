package com.example.obsidianclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class NoteViewModelFactory(
    private val repository: Repository)
    : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")  }}

class NoteViewModel(
    private val repository: Repository
): ViewModel() {

    // _notes doesnt have text values to prevent excessive memory usage
    private val _notes = MutableStateFlow(emptyList<Notes>())
    val notes: Flow<List<Notes>> = _notes.asStateFlow()
    private val _selectedNote = MutableStateFlow<Notes?>(null)
    val selectedNote: StateFlow<Notes?> = _selectedNote
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            val noteList = repository.retrieveNoteList().getOrNull()
            _notes.value = noteList?: emptyList()
        }
    }
    fun createEmptyNote() {
        viewModelScope.launch {
            // title = New Note
            repository.createEmptyNote()
            val noteList = repository.retrieveNoteList().getOrNull()
            _notes.value = noteList?: emptyList()
        }
    }

    fun retrieveFullNote(id: Int) {
        viewModelScope.launch {
            val note = repository.retrieveFullNote(id).getOrNull()
            _selectedNote.value = note
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
        savingNoteTitleJob= viewModelScope.launch {
            delay(200)
            repository.updateNoteTitle(id, title)
        }
    }
}