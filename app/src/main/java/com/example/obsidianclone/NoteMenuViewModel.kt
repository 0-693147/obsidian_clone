package com.example.obsidianclone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    var thisDirectoryId = mutableStateOf<Int?>(null)
    private var _path = mutableStateOf("")
    val thisPath: androidx.compose.runtime.State<String> = _path
    private val _notes = MutableStateFlow(emptyList<LightNote>())
    val notes: Flow<List<LightNote>> = _notes.asStateFlow()

    fun setDirectoryId(directoryId: Int) {
        thisDirectoryId.value = directoryId
    }

    fun setPath() {
        var directoryid: Int? = this.thisDirectoryId.value?: return
        var path = ""
        if (directoryid != null) {
            println("start launch path")
            viewModelScope.launch {
                var i = 0
                while (i < 5 && directoryid != null) {
                    println("while")
                    val directory =
                        repository.retrieveDirectory(directoryId = directoryid!!).getOrNull()
                    println(directory?.name)
                    if (directory == null) break
                    if (directory?.parentId == null) {
                        path += "/" + path
                        break
                    }
                    path = "/" + directory.name + path
                    println("path:")
                    println(path)
                    directoryid = directory.parentId
                    i++
                }
                if (i == 5) {
                    path = "..." + path
                }
                _path.value = path
            }
        }
    }

    fun updateLightNoteList() {
        viewModelScope.launch {
            val noteList = repository.retrieveNoteListLight().getOrNull()
            println("all notes: $noteList")
            if (thisDirectoryId != null) {
                _notes.value = noteList?.filter { it.directoryId == thisDirectoryId.value} ?: emptyList()
            } else {
                _notes.value = noteList?: emptyList()
            }
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
            updateLightNoteList()
        }
    }

    fun createEmptyNote(title: String = "New Note", directoryId: Int) {
        viewModelScope.launch {
            repository.createEmptyNote(title, directoryId)
            updateLightNoteList()
        }
    }

    data class SearchMatch(
        val snippet: String,
        val highlightRange: List<IntRange>
    )
    data class NoteSearchResult(
        val ligthNote: Note,
        val matches: List<SearchMatch>
    )

    private val _searchResults = MutableStateFlow(emptyList<NoteSearchResult>())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    fun launchSearchWithDelay(query: String, beforeContext: Int = 30, afterContext: Int = 30) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(1000)
            searchNotes(query, beforeContext, afterContext)
        }
    }

    fun searchNotes(query: String, beforeContext: Int = 30, afterContext: Int = 30) {
        viewModelScope.launch {
            val fullNotes = repository.retrieveNoteListFull().getOrNull() ?: emptyList<Note>()

            if (fullNotes.isNullOrEmpty()) {
                return@launch
            }
            val queryLow = query.trim().lowercase()
            if (queryLow.isEmpty()) {
                return@launch
            }

            fun findMatchesInText(query: String, fieldText: String?, matches: MutableList<SearchMatch>) {
                if (fieldText.isNullOrEmpty()) return
                val lowerText = fieldText.lowercase()
                var index = lowerText.indexOf(query, 0)
                while (index >= 0) {
                    val startContext = (index - beforeContext).coerceAtLeast(0)
                    val endContext =
                        (index + query.length + afterContext).coerceAtMost(fieldText.length)
                    val snippet = fieldText.substring(startContext, endContext)
                    val matchStartInSnippet = index - startContext
                    val matchEndInSnippet = matchStartInSnippet + query.length
                    matches.add(
                        SearchMatch(
                            snippet = snippet,
                            highlightRange = listOf(matchStartInSnippet until matchEndInSnippet)
                        )
                    )
                    index = lowerText.indexOf(query, index + query.length)
                }
            }

            val localSearchResults = mutableListOf<NoteSearchResult>()

            fullNotes.forEach { note ->
                val matches = mutableListOf<SearchMatch>()
                findMatchesInText(queryLow, note.title, matches)
                findMatchesInText(queryLow, note.text, matches)
                if (!matches.isEmpty()) {
                    localSearchResults.add(
                        NoteSearchResult(
                            ligthNote = Note(note.id, note.title, ""),
                            matches = matches
                        )
                    )
                }
            }

            _searchResults.value = localSearchResults
        }
    }
}

