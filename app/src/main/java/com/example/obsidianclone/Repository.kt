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
    }
