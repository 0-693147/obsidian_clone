package com.example.obsidianclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.obsidianclone.ui.theme.ObsidianCloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getInstance(applicationContext)
        val myDao = db.noteDao()
        val myRepository = Repository(myDao)
        val factory = NoteViewModelFactory(myRepository)
        val view = ViewModelProvider(this, factory)[NoteViewModel::class.java]
        setContent {
            ObsidianCloneTheme() {
                Navigation(view = view)
            }
        }
    }
}



