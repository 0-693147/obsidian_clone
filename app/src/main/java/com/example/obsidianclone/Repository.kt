    package com.example.obsidianclone

    import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


    private val Context.dataStorePreferences by preferencesDataStore(name = "settings")

    class Repository(private val noteDao: NoteDao, private val context: Context) {

        private val dataStore = context.dataStorePreferences

        companion object {
            val FONT_SIZE = intPreferencesKey("font_size")
            val FONT = stringPreferencesKey("font")
        }

        val font: Flow<String> = dataStore.data.map { prefs ->
            prefs[FONT] ?: "Default"
        }

        val fontSize: Flow<Int> = dataStore.data.map { prefs ->
            prefs[FONT_SIZE] ?: 12
        }

        //
        // Settings
        //
        suspend fun setFontSize(size: Int) {
            dataStore.edit { prefs ->
                prefs[FONT_SIZE] = size
            }
        }

        suspend fun setFont(font: String) {
            dataStore.edit { prefs ->
                prefs[FONT] = font
            }
        }
        //
        // Directories
        //
        suspend fun insertDirectory(directory: Directory): Result<Unit> =
            try {
                noteDao.insertDirectory(directory)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun renameDirectory(id: Int, name: String): Result<Unit> =
            try {
                noteDao.renameDirectory(id, name)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun deleteDirectory(id: Int): Result<Unit> =
            try {
                noteDao.deleteDirectory(id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveDirectories(): Result<List<Directory>> =
            try {
                Result.success(noteDao.retrieveDirectories())
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveDirectory(directoryId: Int): Result<Directory> =
            try {
                Result.success(noteDao.retrieveDirectory(directoryId))
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun updateNoteDirectory(noteId: Int, directoryId: Int?): Result<Unit> =
            try {
                noteDao.updateNoteDirectory(noteId, directoryId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun updateDirectoryType(id: Int, hasOnlyNotes: Boolean): Result<Unit> =
            try {
                noteDao.updateDirectoryType(id, hasOnlyNotes)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }


        //
        // Notes
        //
        suspend fun retrieveFullNote(id: Int): Result<Note?> =
            try {
                Result.success(noteDao.retrieveFullNote(id))
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun createEmptyNote(title: String = "New Note", directoryId: Int): Result<Unit> =
            try {
                noteDao.newNote(title = title, directoryId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun updateNoteText(id: Int, text: String): Result<Unit> =
            try {
                noteDao.updateText(id, text)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun updateNoteTitle(id: Int, title: String): Result<Unit> =
            try {
                noteDao.updateTitle(id, title)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun deleteNote(id: Int): Result<Unit> =
            try {
                noteDao.deleteNote(id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveNoteListLight(): Result<List<LightNote>> =
            try {
                val lightNotes = noteDao.retrieveNoteListLight()?: emptyList()
                Result.success(lightNotes)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveNoteListFull(): Result<List<Note>> =
            try {
                val notes = noteDao.retrieveNoteListFull()?: emptyList()
                Result.success(notes)
            } catch (e: Exception) {
                Result.failure(e)
            }


        //
        // Note Assets
        //
        suspend fun retrieveAsset(id: Int): Result<NoteAsset> =
            try {
                val asset = noteDao.retrieveAsset(id)
                Result.success(asset)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveNoteAssetsByType(id: Int, type: Int): Result<List<NoteAsset>> =
            try {
                val assets = noteDao.retrieveNoteAssetsByType(id, type)
                Result.success(assets)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveNoteAssets(id: Int): Result<List<NoteAsset>> =
            try {
                val allAssets = noteDao.retrieveNoteAssets(id)
                Result.success(allAssets)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun createNoteAsset(asset: NoteAsset): Result<Unit> =
            try {
                noteDao.writeAsset(asset)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun deleteNoteAsset(assetId: Int): Result<Unit> =
        try {
            noteDao.deleteAsset(assetId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

        //
        // Graph
        //
        suspend fun getNodeList(): Result<List<NodeConnection>> =
        try {
            val noteConnectionList = noteDao.retrieveNodeConnectionList()
            Result.success(noteConnectionList)
        } catch (e: Exception) {
            Result.failure(e)
        }

        suspend fun createNodeConnection(thisNoteId: Int, otherNoteId: Int): Result<Unit> =
            try {
                noteDao.createNodeConnection(thisNoteId, otherNoteId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun deleteNodeConnection(thisNoteId: Int, otherNoteId: Int): Result<Unit> =
            try {
                noteDao.deleteNodeConnection(thisNoteId, otherNoteId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveConnectionsByNode(thisNoteId: Int): Result<List<NodeConnection>?> =
            try {
                val connectionList = noteDao.retrieveNoteConnections(thisNoteId)
                Result.success(connectionList)
            } catch (e: Exception) {
                Result.failure(e)
            }

    }
