package com.example.obsidianclone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


class DirectoryScreenViewModelFactory(
    private val repository: Repository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DirectoryScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DirectoryScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")  }}



@Serializable
private data class DirectoryItemDTO(
    val name: String,
    val level: Int,
    val hasOnlyNotes: Boolean,
    val children: List<DirectoryItemDTO>
)


data class DirectoryNode(
    val directory: Directory,
    val children: List<DirectoryNode>,
    val notes: List<Note>
) {
    fun flatten(depth: Int = 0): List<Pair<Int, DirectoryNode>> {
        return listOf(depth to this) + children.flatMap { child ->
            child.flatten(depth + 1)
        }
    }
}



class DirectoryScreenViewModel(
    private val repository: Repository
) : ViewModel() {
    var isRenameCardVisible by mutableStateOf(false)
    var focusedDirectory by mutableStateOf<DirectoryNode?>(null)

    private val _tree = MutableStateFlow<DirectoryNode?>(null)
    val tree: StateFlow<DirectoryNode?> = _tree.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = repository.retrieveDirectories().getOrNull()
            if (existing.isNullOrEmpty()) {
                repository.insertDirectory(Directory(name = "root", parentId = null))
                val root = repository.retrieveDirectories().getOrNull()?.first() ?: return@launch
                repository.insertDirectory(Directory(name = "All Notes", parentId = root.id))
                val allNotes = repository.retrieveDirectories().getOrNull()
                    ?.find { it.name == "All Notes" } ?: return@launch
                repository.insertDirectory(Directory(name = "My Notes", parentId = allNotes.id, hasOnlyNotes = true))
            }
            loadTree()
        }
    }

    private fun loadTree() {
        viewModelScope.launch {
            val directories = repository.retrieveDirectories().getOrNull() ?: return@launch
            val notes = repository.retrieveNoteListFull().getOrNull() ?: return@launch
            val root = directories.find { it.parentId == null } ?: return@launch
            _tree.value = buildNode(root, directories, notes)
        }
    }

    private fun buildNode(
        directory: Directory,
        directories: List<Directory>,
        notes: List<Note>
    ): DirectoryNode {
        return DirectoryNode(
            directory = directory,
            children = directories
                .filter { it.parentId == directory.id }
                .map { buildNode(it, directories, notes) },
            notes = notes.filter { it.directoryId == directory.id }
        )
    }

    fun createDirectory(name: String, parentId: Int? = null) {
        viewModelScope.launch {
            repository.insertDirectory(Directory(name = name, parentId = parentId))
            loadTree()
        }
    }

    fun renameDirectory(id: Int, name: String) {
        viewModelScope.launch {
            repository.renameDirectory(id, name)
            loadTree()
        }
    }

    fun deleteDirectory(id: Int) {
        viewModelScope.launch {
            repository.deleteDirectory(id)
            loadTree()
        }
    }

    fun moveNote(noteId: Int, directoryId: Int?) {
        viewModelScope.launch {
            repository.updateNoteDirectory(noteId, directoryId)
            loadTree()
        }
    }

    fun updateDirectoryType(id: Int, hasOnlyNotes: Boolean) {
        viewModelScope.launch {
            repository.updateDirectoryType(id, hasOnlyNotes)
            loadTree()
        }
    }
}
