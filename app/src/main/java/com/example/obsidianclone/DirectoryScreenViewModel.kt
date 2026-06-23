package com.example.obsidianclone

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class DirectoryItem(
    name: String,
    var level: Int,
    var hasOnlyNotes: Boolean,
    var parent: DirectoryItem?,
    val children: SnapshotStateList<DirectoryItem> = mutableStateListOf()
) {
    var name by mutableStateOf(name)

    fun addSubdirectory(directory: DirectoryItem, place: Int? = null) {
        println(parent)
        val directoryCopy = directory
        directoryCopy.parent = this
        if (place != null) {
            this.children.add(place, directoryCopy)
        } else {
            this.children.add(directoryCopy)
        }
        println(parent)
    }
    fun moveSubdirectory(directory: DirectoryItem, place: Int) {
        val directoryCopy = directory
        this.children.add(place, directoryCopy)
        this.children.remove(directory)
    }
    fun delete() {
        println(parent)
        this.parent?.children?.remove(this)
        println(parent)
    }
    fun rename(name: String) {
        this.name = name
    }
    override fun toString(): String {
        if (this.children.isEmpty()) return "    ".repeat(this.level) + this.name + "\n"
        val directoryList = this.children.fold(""){
                acc, directory -> acc + directory
        }
        val thisDirectory = "    ".repeat(this.level) + this.name + "\n"
        return thisDirectory + directoryList
    }
    fun numberOfSubdirectories(): Int {
        if (this.children.isEmpty()) return 1
        val numberOfSubdirectories = this.children.fold(0){ acc, directory ->
            acc + directory.numberOfSubdirectories()
        }
        return numberOfSubdirectories
    }
    fun flatten(): List<DirectoryItem> {
        if (this.children.isEmpty()) return listOf(this)
        val numberOfSubdirectories = this.children.fold(listOf<DirectoryItem>()){ acc, directory ->
            acc + directory.flatten()
        }
        return listOf(this) + numberOfSubdirectories
    }
}

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


class DirectoryScreenViewModel(
    private val repository: Repository
): ViewModel() {
    var isRenameCardVisible by mutableStateOf(false)
    var focusedDirectory by mutableStateOf<DirectoryItem?>(null)
    var root by mutableStateOf<DirectoryItem?>(null)


    private fun DirectoryItem.toDTO(): DirectoryItemDTO = DirectoryItemDTO(
        name = this.name,
        level = this.level,
        hasOnlyNotes = this.hasOnlyNotes,
        children = this.children.map { it.toDTO() }
    )

    private fun DirectoryItemDTO.toDirectoryItem(parent: DirectoryItem? = null): DirectoryItem {
        val item = DirectoryItem(
            name = this.name,
            level = this.level,
            hasOnlyNotes = this.hasOnlyNotes,
            parent = parent,
        )
        this.children.forEach {
            item.children.add(it.toDirectoryItem(parent = item))
        }
        return item
    }
    fun DirectoryItem.toJson(): String = Json.encodeToString(this.toDTO())
    fun String.toDirectoryItem(): DirectoryItem = Json.decodeFromString<DirectoryItemDTO>(this).toDirectoryItem()

    fun saveDirectoryStructure(context: Context) {
        val rootInstance = root
        if (rootInstance != null) {
            val json = rootInstance.toJson()
            context.openFileOutput("directories.json", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        }
    }

    fun loadDirectoryStructure(context: Context): DirectoryItem? {
        return try {
            val json = context.openFileInput("directories.json").bufferedReader().use {
                it.readText()
            }
            json.toDirectoryItem()
        } catch (e: Exception) {
            null
        }
    }
}

