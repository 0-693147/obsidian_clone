package com.example.obsidianclone

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase


@Entity
data class Notes (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val text: String = "",
)

data class LightNotes (
    val id: Int = 0,
    val title: String = "",
)


@Dao
interface NoteDao {
    @Query("INSERT INTO Notes (title, text) VALUES (:title, :text)")
    suspend fun newNote(title: String = "", text: String = "")

    @Query("UPDATE Notes SET title = :title  WHERE id = :id")
    suspend fun updateTitle(id: Int, title: String)

    @Query("UPDATE Notes SET text = :text WHERE id = :id")
    suspend fun updateText(id: Int, text: String)

    @Query("DELETE FROM Notes WHERE id = :id")
    suspend fun deleteNote(id: Int)

    @Query("SELECT id, title, '' FROM Notes")
    suspend fun retrieveNoteListLight(): List<LightNotes>

    @Query("SELECT * FROM Notes WHERE id = :id")
    suspend fun retrieveFullNote(id: Int): Notes
}


@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "obsidian_clone_db"
                ).build().also { INSTANCE = it }
            }
    }
}
