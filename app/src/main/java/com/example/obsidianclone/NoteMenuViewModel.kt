package com.example.obsidianclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class NoteMenuViewModelFactory(
    private val repository: Repository)
    : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteMenuViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteMenuViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")  }}

class NoteMenuViewModel(
    private val repository: Repository
): ViewModel() {

    // _notes doesnt have text values to prevent excessive memory usage
    private val _notes = MutableStateFlow(emptyList<Notes>())
    val notes: Flow<List<Notes>> = _notes.asStateFlow()

    init {
        updateLightNoteList()
    }

    fun updateLightNoteList() {
        viewModelScope.launch {
            val noteList = repository.retrieveNoteList().getOrNull()
            _notes.value = noteList?: emptyList()
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
            updateLightNoteList()
        }
    }

    fun createEmptyNote() {
        viewModelScope.launch {
            repository.createEmptyNote()
            updateLightNoteList()
        }
    }
}

