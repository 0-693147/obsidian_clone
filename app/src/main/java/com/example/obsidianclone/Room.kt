package com.example.obsidianclone

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert


object Converters {
    @TypeConverter
    @JvmStatic
    fun fromUri(uri: Uri?): String? = uri?.toString()

    @TypeConverter
    @JvmStatic
    fun toUri(value: String?): Uri? = value?.let { Uri.parse(it) }
}

@Entity
data class Notes (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val text: String = "",
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Notes::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("noteId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteAssets (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,
    val contentType: Int,
    val link: Uri,
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

    @Query("SELECT * FROM NoteAssets WHERE id = :id")
    suspend fun retrieveAsset(id: Int): NoteAssets

    @Upsert
    suspend fun writeAsset(asset: NoteAssets)

    @Query("SELECT * FROM NoteAssets WHERE noteId = :id AND contentType = :type")
    suspend fun retrieveNoteAssetsByType(id: Int, type: Int): List<NoteAssets>

    @Query("SELECT * FROM NoteAssets WHERE noteId = :id")
    suspend fun retrieveNoteAssets(id: Int): List<NoteAssets>

    @Query("DELETE FROM NoteAssets WHERE id = :assetId")
    suspend fun deleteAsset(assetId: Int)
}


@Database(
    entities = [Notes::class, NoteAssets::class],
    version = 1,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "obsidian_db_images"
                ).build().also { INSTANCE = it }
            }
    }
}
