    package com.example.obsidianclone


    class Repository(private val noteDao: NoteDao) {
        suspend fun retrieveFullNote(id: Int): Result<Note?> =
            try {
                Result.success(noteDao.retrieveFullNote(id))
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun createEmptyNote(title: String = "New Note"): Result<Unit> =
            try {
                noteDao.newNote(title = title)
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

        suspend fun retrieveNoteListLight(): Result<List<Note>> =
            try {
                val lightNotes = noteDao.retrieveNoteListLight()?: emptyList()
                Result.success(lightNotes.map { lightNote ->
                    Note(lightNote.id, lightNote.title, "")
                })
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
