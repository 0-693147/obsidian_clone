package com.example.obsidianclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class GraphScreenModelFactory(
    private val repository: Repository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraphScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GraphScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")  }}

class GraphScreenViewModel(
    private val repository: Repository
): ViewModel() {

    data class GraphNodeLight(
        val note: LightNote,
        val neighbors: MutableSet<Int>
    )

    private val _adjacencyMatrix = MutableStateFlow<Map<Int, GraphNodeLight>>(emptyMap())
    val adjacencyMatrix = _adjacencyMatrix.asStateFlow()

    init {
        viewModelScope.launch {
            buildAdjacencyMatrix()
        }
    }
    fun buildAdjacencyMatrix() {
        viewModelScope.launch {
            val connections = repository.getNodeList().getOrNull() ?: return@launch
            val noteList = repository.retrieveNoteListLight().getOrNull() ?: return@launch
            val noteListLight = noteList.map { note ->
                LightNote(note.id, note.title)
            }
            val graphNodes = (noteListLight.map{ note ->
                note.id to GraphNodeLight(
                    note = note,
                    neighbors = mutableSetOf()
                )}).toMap()

            for (connection in connections) {
                graphNodes[connection.thisNoteId]?.neighbors?.add(connection.otherNoteId)
                graphNodes[connection.otherNoteId]?.neighbors?.add(connection.thisNoteId)
            }
            _adjacencyMatrix.value = graphNodes
        }
    }
}
