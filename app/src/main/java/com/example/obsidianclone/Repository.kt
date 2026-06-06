    package com.example.obsidianclone


    class Repository(private val noteDao: NoteDao) {
        suspend fun retrieveFullNote(id: Int): Result<Notes?> =
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

        suspend fun retrieveNoteList(): Result<List<Notes>> =
            try {
                val lightNotes = noteDao.retrieveNoteListLight()?: emptyList()
                Result.success(lightNotes.map { lightNote ->
                    Notes(lightNote.id, lightNote.title, "")
                })
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveAsset(id: Int): Result<NoteAssets> =
            try {
                val asset = noteDao.retrieveAsset(id)
                Result.success(asset)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveNoteAssetsByType(id: Int, type: Int): Result<List<NoteAssets>> =
            try {
                val assets = noteDao.retrieveNoteAssetsByType(id, type)
                Result.success(assets)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun retrieveNoteAssets(id: Int): Result<List<NoteAssets>> =
            try {
                val allAssets = noteDao.retrieveNoteAssets(id)
                Result.success(allAssets)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun createNoteAsset(asset: NoteAssets): Result<Unit> =
            try {
                noteDao.writeAsset(asset)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun deleteNoteAsset(assetId: Int): Result<Unit> =
        try {
            println("123412341 repository: using delete dao")
            println("asset id " + assetId.toString())
            noteDao.deleteAsset(assetId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
