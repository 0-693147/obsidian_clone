# Obsidian Clone

This is a clone of the Obsidian android app with limited functionality made for a university project

## Functionality

- Create, store and edit notes
- Add images, video and audio files
- A directory hierarchy for note grouping
- Graph view for visualization

## Technologies

- Language: Kotlin
- Framework: Jetpack Compose
- Architecture: MVVM (6 Screens, 5 ViewModels, 1 Repository, 1 Database)
- Database: Room (SQLite)

## ViewModels

The project has 5 ViewModels:

### DirectoryScreenViewModel
**Role:** Creation, deletion and renaming of directories, retrieval of directories from the repository, building the tree hierarchy and flattening it for display on the screen.

**Functions:**
- `createDirectory(name, parentId)` — inserts a new directory under the given parent and reloads the tree
- `renameDirectory(id, name)` — updates a directory's name and reloads the tree
- `deleteDirectory(id)` — removes a directory and reloads the tree
- `moveNote(noteId, directoryId)` — reassigns a note to a different directory
- `updateDirectoryType(id, hasOnlyNotes)` — toggles whether a directory holds only notes or can also contain subdirectories
- `loadTree()` *(private)* — fetches all directories and notes from the repository and builds a `DirectoryNode` tree from the root down
- `buildNode()` *(private)* — recursively constructs a `DirectoryNode` by attaching child directories and notes to each node

**Key state:**
- `tree: StateFlow<DirectoryNode?>` — the full directory hierarchy, observed by the UI
- `focusedDirectory` — the currently selected directory node (used for rename/delete actions)
- `isRenameCardVisible` — controls visibility of the rename dialog

**On init:** seeds the database with a default `root → All Notes → My Notes` structure if no directories exist yet.

### NoteMenuViewModel
**Role:** Managing and displaying notes within a directory, including listing, creation, deletion, path resolution, and full-text search. It manages both the NoteMenu screen and the Search screen.

**Functions:**
- `setDirectoryId(directoryId)` — sets the currently active directory
- `setPath()` — walks up the directory tree (up to 5 levels) to build a breadcrumb path string, stored in `thisPath`
- `updateLightNoteList()` — fetches the lightweight note list from the repository and filters it by the current directory
- `deleteNote(id)` — deletes a note by ID and refreshes the note list
- `createEmptyNote(title, directoryId)` — creates a blank note in the given directory and refreshes the note list
- `searchNotes(query, beforeContext, afterContext)` — searches all notes by title and body, returning snippets with highlighted match ranges
- `launchSearchWithDelay(query)` — debounced wrapper around `searchNotes` with a 1 second delay, cancelling any previous pending search

**Key state:**
- `notes: Flow<List<LightNote>>` — filtered list of notes for the current directory, observed by the UI
- `thisPath: State<String>` — breadcrumb path string for the current directory (e.g. `/root/All Notes/My Notes`)
- `thisDirectoryId` — the currently active directory ID
- `searchResults: StateFlow<List<NoteSearchResult>>` — list of search hits, each containing the matched note and annotated snippets with highlight ranges


### NoteEditViewModel
**Role:** Managing the state of an open note during editing, including loading, saving, asset management, and maintaining inter-note link connections.

**Functions:**
- `retrieveFullNote(id)` — loads a full note by ID into `selectedNote`, toggling `selectedNoteLoaded` before and after
- `updateNoteText(id, text)` — debounced (200ms) save of the note body to the repository
- `updateNoteTitle(id, title)` — debounced (200ms) save of the note title to the repository
- `retrieveNoteAssets(id)` — fetches all media assets attached to the current note
- `createNoteAsset(noteId, type, link)` — attaches a new media asset (image, file, etc.) to the note by URI and refreshes the asset list
- `deleteNoteAsset(assetId, noteId)` — removes an asset by ID and refreshes the asset list
- `retrieveAssetsByType(id, type)` — fetches assets filtered by content type
- `retrieveNoteList()` — loads a lightweight list of notes from the same directory as the current note (used for the note link picker)
- `handleLinks(noteLinkList)` — debounced (2000ms) sync of inter-note connections; creates new connections for newly added `[[links]]` and deletes connections for removed ones

**Key state:**
- `selectedNote: StateFlow<Note?>` — the currently open full note, observed by the editor UI
- `selectedNoteLoaded: MutableStateFlow<Boolean>` — signals when the note has finished loading
- `noteAssets: StateFlow<List<NoteAsset>>` — media assets attached to the current note
- `notes` — lightweight note list for the current directory, used to resolve note link targets
- `isNoteLinkPickerVisible` — controls visibility of the note link picker dialog

### GraphScreenViewModel
**Role:** Building and exposing a note connection graph as an adjacency map for display on the graph screen.

**Functions:**
- `buildAdjacencyMatrix()` — fetches all note connections and the full note list from the repository, constructs a map of node ID to `GraphNodeLight` (note + its neighbor IDs), and populates edges bidirectionally

**Key state:**
- `adjacencyMatrix: StateFlow<Map<Int, GraphNodeLight>>` — the full graph as an adjacency map, where each entry holds a lightweight note and the set of IDs of notes it is connected to

**On init:** immediately calls `buildAdjacencyMatrix()` to populate the graph on screen entry.

### SettingsViewModel
**Role:** Managing user preferences for note display, specifically font size and font family, persisted via the repository.

**Functions:**
- `setFontSize(size)` — persists the selected font size to the repository
- `setFont(font)` — persists the selected font family to the repository

**Key state:**
- `fontSize: StateFlow<Int>` — the current font size setting, defaulting to `12`, active while the UI is subscribed
- `font: StateFlow<String>` — the current font family setting, defaulting to `"Default"`, active while the UI is subscribed


## Repository
The single data layer for the app, bridging together the ViewModels with the Room database (`NoteDao`) and DataStore preferences. All operations return `Result<T>` for safe error handling. The repository is divided into 5 sections:

- **Settings** — reads and writes font size and font family via DataStore, exposed as `Flow` for reactive UI updates
- **Directories** — CRUD operations on the directory tree, including retrieval of single or all directories and updating directory type
- **Notes** — creation, deletion, and updating of notes (title, body, directory), plus retrieval in both lightweight (`LightNote`) and full (`Note`) form
- **Assets** — attaching, retrieving, and deleting media assets linked to notes, with support for filtering by content type
- **Graph** — managing inter-note connections (`NodeConnection`), including creation, deletion, and retrieval by node

## Database
Built with **Room**, the database (`obsidian_db2`) consists of four tables and a type converter for `Uri` serialization.

### Entities

**`Directory`** — represents a folder in the directory tree. Holds a `name`, an optional `parentId` (self-referential, for nesting), and a `hasOnlyNotes` flag that distinguishes leaf directories (note containers) from branch directories (folder containers). Supports cascading null on directory deletion.

**`Note`** — represents a single note with a `title`, a `text` body, and an optional `directoryId` foreign key linking it to a `Directory`. The directory foreign key is set to null on directory deletion, orphaning the note rather than deleting it.

**`NoteAsset`** — represents a media file attached to a note. Stores a `contentType` integer and a `link` (`Uri`, serialized to string via `Converters`). Cascades delete and update from the parent `Note`.

**`NodeConnection`** — represents a directed link between two notes (`thisNoteId → otherNoteId`), used to build the note graph. The combination of both IDs is unique (enforced by an index). Both foreign keys cascade on update and delete from `Note`.

**`LightNote`** *(not a table)* — a lightweight projection of `Note` used for list display, containing only `id`, `title`, `directoryId`, and a 100-character `snippet` of the body.

### Other
- **`Converters`** — a `@TypeConverter` pair that serializes `Uri` to `String` and back, required for storing asset links in Room
- **`AppDatabase`** — singleton Room database instance, exposing `NoteDao`
