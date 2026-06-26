package com.example.obsidianclone

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
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


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Directory::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("directoryId"),
            onDelete = ForeignKey.SET_NULL
        ),
    ]
)
data class Directory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val parentId: Int? = null,
    val directoryId: Int? = null,
    val hasOnlyNotes: Boolean = false
)

@Entity (
    foreignKeys = [
        ForeignKey(
            entity = Directory::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("directoryId"),
            onDelete = ForeignKey.SET_NULL
        ),
    ]
)
data class Note (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val text: String = "",
    val directoryId: Int? = null
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
    val directoryId: Int? = null,
    val snippet: String = ""
)


@Dao
interface NoteDao {
    @Insert
    suspend fun insertDirectory(directory: Directory)

    @Query("UPDATE Directory SET name = :name WHERE id = :id")
    suspend fun renameDirectory(id: Int, name: String)

    @Query("DELETE FROM Directory WHERE id = :id")
    suspend fun deleteDirectory(id: Int)

    @Query("SELECT * FROM Directory")
    suspend fun retrieveDirectories(): List<Directory>

    @Query("SELECT * FROM Directory WHERE directoryId = :directoryId")
    suspend fun retrieveDirectory(directoryId: Int): Directory

    @Query("UPDATE Note SET directoryId = :directoryId WHERE id = :noteId")
    suspend fun updateNoteDirectory(noteId: Int, directoryId: Int?)

    @Query("UPDATE Directory SET hasOnlyNotes = :hasOnlyNotes WHERE id = :id")
    suspend fun updateDirectoryType(id: Int, hasOnlyNotes: Boolean)

    @Query("INSERT INTO Note (title, text, directoryId) VALUES (:title, :text, :directoryId)")
    suspend fun newNote(title: String = "", directoryId: Int, text: String = "")

    @Query("UPDATE Note SET title = :title  WHERE id = :id")
    suspend fun updateTitle(id: Int, title: String)

    @Query("UPDATE Note SET text = :text WHERE id = :id")
    suspend fun updateText(id: Int, text: String)

    @Query("DELETE FROM Note WHERE id = :id")
    suspend fun deleteNote(id: Int)

    @Query("SELECT id, title, directoryId, SUBSTR(text, 1, 100) as snippet FROM Note")
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
    entities = [Note::class, NoteAsset::class, NodeConnection::class, Directory::class],
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
                    "obsidian_db2"
                ).build().also { INSTANCE = it }
            }
    }
}
