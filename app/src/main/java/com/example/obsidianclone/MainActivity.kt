package com.example.obsidianclone

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.obsidianclone.ui.theme.ObsidianCloneTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getInstance(applicationContext)
        val myDao = db.noteDao()
        val myRepository = Repository(myDao, this)
        val factoryMenu = NoteMenuViewModelFactory(myRepository)
        val factoryEdit = NoteEditModelFactory(myRepository)
        val factoryGraph = GraphScreenModelFactory(myRepository)
        val factoryDirectory = DirectoryScreenViewModelFactory(myRepository)
        val viewMenu = ViewModelProvider(this, factoryMenu)[NoteMenuViewModel::class.java]
        val viewEdit = ViewModelProvider(this, factoryEdit)[NoteEditViewModel::class.java]
        val viewGraph = ViewModelProvider(this, factoryGraph)[GraphScreenViewModel::class.java]
        val viewDirectory = ViewModelProvider(this, factoryDirectory)[DirectoryScreenViewModel::class.java]
        val viewSettings = SettingsViewModel(myRepository)
        setContent {
            ObsidianCloneTheme() {
                Navigation(
                    viewMenu = viewMenu,
                    viewEdit = viewEdit,
                    viewGraph = viewGraph,
                    viewDirectory = viewDirectory,
                    viewSettings = viewSettings
                )
            }
        }
    }
}



