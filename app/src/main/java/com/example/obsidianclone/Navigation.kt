package com.example.obsidianclone

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.obsidianclone.screens.NoteEditor
import com.example.obsidianclone.screens.NotesScreen
import kotlinx.serialization.Serializable


//sealed class Routes(val route: String) {
//    data object NotesScreen: Routes("NotesScreen")
//    data object NoteEditor: Routes("NoteEditor")
//}

@Serializable
object NoteScreenRoute

@Serializable
data class NoteRoute(val id: Int)

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    view: NoteViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NoteScreenRoute
    ) {
        composable<NoteScreenRoute>{
            NotesScreen(navController, view)
        }
        composable<NoteRoute>{ backStackEntry ->
            val gotoNote: NoteRoute = backStackEntry.toRoute()
            NoteEditor(navController, view = view, id = gotoNote.id)
        }
    }
}