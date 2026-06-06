package com.example.obsidianclone

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.obsidianclone.screens.NoteEditor
import com.example.obsidianclone.screens.NoteMenu
import com.example.obsidianclone.screens.SearchScreen
import com.example.obsidianclone.screens.SettingsScreen
import kotlinx.serialization.Serializable


//sealed class Routes(val route: String) {
//    data object NotesScreen: Routes("NotesScreen")
//    data object NoteEditor: Routes("NoteEditor")
//}

@Serializable
object NoteMenuRoute


@Serializable
data class NoteRoute(val id: Int)

@Serializable
object SettingsScreenRoute

@Serializable
object SearchScreenRoute

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    viewMenu: NoteMenuViewModel,
    viewEdit: NoteEditViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NoteMenuRoute
    ) {
        composable<NoteMenuRoute>{
            NoteMenu(navController, viewMenu)
        }
        composable<NoteRoute>{ backStackEntry ->
            val gotoNote: NoteRoute = backStackEntry.toRoute()
            NoteEditor(navController, view = viewEdit, thisNoteId = gotoNote.id)
        }
        composable<SettingsScreenRoute>{
            SettingsScreen()
        }
        composable<SearchScreenRoute>{
            SearchScreen(viewMenu, navController)
        }
    }
}