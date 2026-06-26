package com.example.obsidianclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SettingsViewModel(private val repository: Repository) : ViewModel() {
    val fontSize = repository.fontSize.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        12
    )

    val font = repository.font.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Default"
    )

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            repository.setFontSize(size)
        }
    }

    fun setFont(font: String) {
        viewModelScope.launch {
            repository.setFont(font)
        }
    }
}