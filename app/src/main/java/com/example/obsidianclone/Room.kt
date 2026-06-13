package com.example.obsidianclone

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
data class Note (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val text: String = "",
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("noteId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteAsset (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,
    val contentType: Int,
    val link: Uri,
)

@Entity(
    indices = [Index(value = ["thisNoteId", "otherNoteId"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("thisNoteId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey( entity = Note::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("otherNoteId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
)
    ]
)
data class NodeConnection(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val thisNoteId: Int,
    val otherNoteId: Int,
)


data class LightNote (
    val id: Int = 0,
    val title: String = "",
)


@Dao
interface NoteDao {
    @Query("INSERT INTO Note (title, text) VALUES (:title, :text)")
    suspend fun newNote(title: String = "", text: String = "")

    @Query("UPDATE Note SET title = :title  WHERE id = :id")
    suspend fun updateTitle(id: Int, title: String)

    @Query("UPDATE Note SET text = :text WHERE id = :id")
    suspend fun updateText(id: Int, text: String)

    @Query("DELETE FROM Note WHERE id = :id")
    suspend fun deleteNote(id: Int)

    @Query("SELECT id, title, '' FROM Note")
    suspend fun retrieveNoteListLight(): List<LightNote>

    @Query("SELECT id, title, text FROM Note")
    suspend fun retrieveNoteListFull(): List<Note>

    @Query("SELECT * FROM Note WHERE id = :id")
    suspend fun retrieveFullNote(id: Int): Note

    @Query("SELECT * FROM NoteAsset WHERE id = :id")
    suspend fun retrieveAsset(id: Int): NoteAsset

    @Upsert
    suspend fun writeAsset(asset: NoteAsset)

    @Query("SELECT * FROM NoteAsset WHERE noteId = :id AND contentType = :type")
    suspend fun retrieveNoteAssetsByType(id: Int, type: Int): List<NoteAsset>

    @Query("SELECT * FROM NoteAsset WHERE noteId = :id")
    suspend fun retrieveNoteAssets(id: Int): List<NoteAsset>

    @Query("DELETE FROM NoteAsset WHERE id = :assetId")
    suspend fun deleteAsset(assetId: Int)

    @Query("SELECT * FROM NodeConnection")
    suspend fun retrieveNodeConnectionList(): List<NodeConnection>

    @Query("INSERT INTO NodeConnection (thisNoteId, otherNoteId) VALUES (:thisNoteId, :otherNoteId)")
    suspend fun createNodeConnection(thisNoteId: Int, otherNoteId: Int)

    @Query("DELETE FROM NodeConnection WHERE thisNoteId = :thisNoteId AND otherNoteId = :otherNoteId")
    suspend fun deleteNodeConnection(thisNoteId: Int, otherNoteId: Int)

    @Query("SELECT * FROM NodeConnection WHERE thisNoteId = :thisNoteId")
    suspend fun retrieveNoteConnections(thisNoteId: Int): List<NodeConnection>
}



@Database(
    entities = [Note::class, NoteAsset::class, NodeConnection::class],
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
                    "obsidian_db1"
                ).build().also { INSTANCE = it }
            }
    }
}
