package jehr.projects.musicus

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/*Music-related data*/
@Serializable
data class Duration(
    var hours: Int = 0,
    var minutes: Int = 0,
    var seconds: Int = 0,
    var ms: Int = 0
) {

    override fun toString(): String =
        "${if (this.hours != 0) this.hours.toString() + ":" else ""}${if (this.minutes != 0) this.minutes.toString() + ":" else "00"}${if (this.seconds != 0) (if (this.seconds < 10) "0" + this.seconds.toString() else this.seconds) else "00"}"

    fun toInt(): Int =
        this.ms + (this.seconds * 1000) + (this.minutes * 60000) + (this.hours * 360000)

    fun toStringWithMs(): String = "${this}${if (this.ms != 0) "." + this.ms.toString() else ""}"

    companion object {
        fun breakDownMs(ms: Double): List<Int> {
            val millis = ms % 1000
            var sec = (ms - (ms % 1000)) / 1000
            var min = (sec - (sec % 60)) / 60
            sec -= min * 60
            val hrs = (min - (min % 60)) / 60
            min -= hrs * 60
            return listOf(hrs.toInt(), min.toInt(), sec.toInt(), millis.toInt())
        }
    }

    fun decrement(ms: Double) {
        val time = breakDownMs(ms)
        this.hours -= time[0]
        this.minutes -= time[1]
        this.seconds -= time[2]
        this.ms -= time[3]
    }

    fun increment(ms: Double) = this.decrement(-ms)
}

@Serializable
data class MusicusDate(val day: Int, val month: Int, val year: Int)

enum class LyricsType {
    PLAINTEXT, LRC, ELRC;

    companion object {
        fun detectType(lrc: File): LyricsType {
            TODO()
        }
    }
}

data class Lyrics(
    val path: String,
    var type: LyricsType = LyricsType.PLAINTEXT,
    var autoType: LyricsType = LyricsType.PLAINTEXT,
    var visible: Boolean = true,
    var lrcFile: File? = null
) {
    fun build() {
        this.lrcFile = File(this.path)
    }

    fun toSkeleton() = LyricsSkeleton(this.path, this.type, this.autoType, this.visible)
}
@Serializable
data class LyricsSkeleton(val path: String, val type: LyricsType, val autoType: LyricsType, val visible: Boolean) {
    fun toLyrics() = Lyrics(this.path, this.type, this.autoType, this.visible)
}

class LangList(
    var langList: MutableList<String> = mutableListOf("<unknown>"),
    var default: MutableList<String> = mutableListOf("<unknown>")
) {
    fun createIfNotFound(key: String): String {
        if (key !in this.langList) {
            this.langList.add(key)
        }
        return key
    }
}
val langList = LangList()

fun bitmapFromPath(path: String?): ImageBitmap? {
    if (path == null) return null
    val imgFile = File(path)
    return if (imgFile.exists()) {
        BitmapFactory.decodeFile(imgFile.absolutePath).asImageBitmap()
    } else null
}

data class Track(
    val path: String,
    var name: String,
    var originalName: String? = null,
    var lang: MutableList<String> = langList.default,
    var origin: String = "Single",
    var originInfo: String? = null,
    var runtime: Duration = Duration(),
    var cover: Boolean = false,
    var originalLang: MutableList<String> = langList.default,
    var album: String? = null,
    var playlists: MutableList<String> = mutableListOf(),
    var artists: MutableList<String> = mutableListOf(),
    var imgPath: String? = null,
    val dateAdded: MusicusDate? = null,
    var timesPlayed: Int = 0,
    var stdLyrics: Lyrics? = null,
    var trnLyrics: Lyrics? = null,
    var romLyrics: Lyrics? = null,
    var selectedLyrics: Int = 0,
    var file: File? = null
) {
    var isBuilt = false

    fun build() {
        this.stdLyrics?.build()
        this.trnLyrics?.build()
        this.romLyrics?.build()
        this.file = File(this.path)
        this.isBuilt = true
    }

    fun close() {
        this.file = null
        this.isBuilt = false
    }

    fun toSkeleton() = TrackSkeleton(this.path, this.name, this.originalName, this.lang, this.origin, this.originInfo, this.runtime, this.cover, this.originalLang, this.album, this.playlists, this.artists, this.imgPath, this.dateAdded, this.timesPlayed, this.stdLyrics?.toSkeleton(), this.trnLyrics?.toSkeleton(), this.romLyrics?.toSkeleton(), this.selectedLyrics)
}
@Serializable
data class TrackSkeleton(val path: String, val name: String, val originalName: String?, val lang: MutableList<String>, val origin: String, val originInfo: String?, val runtime: Duration, val cover: Boolean, val originalLang: MutableList<String>, val album: String?, val playlists: MutableList<String>, val artists: MutableList<String>, val imgPath: String?, val dateAdded: MusicusDate?, val timesPlayed: Int, val stdLyrics: LyricsSkeleton?, val trnLyrics: LyricsSkeleton?, val romLyrics: LyricsSkeleton?, val selectedLyrics: Int) {

    fun toTrack() = Track(path = this.path, name = this.name, originalName = this.originalName, lang = this.lang, origin = this.origin, originInfo = this.originInfo, runtime = this.runtime, cover = this.cover, originalLang = this.originalLang, album = this.album, playlists = this.playlists, artists = this.artists, imgPath = this.imgPath, dateAdded = this.dateAdded, timesPlayed = this.timesPlayed, stdLyrics = this.stdLyrics?.toLyrics(), trnLyrics = this.trnLyrics?.toLyrics(), romLyrics = this.romLyrics?.toLyrics(), selectedLyrics = this.selectedLyrics, file = null
    )
}

data class Playlist(var tracks: MutableList<Track> = mutableListOf(), var imgPath: String? = null, var primaryArtist: String? = null, var name: String = "<unknown>") {
    fun toSkeleton() = SongCollectionSkeleton(this.imgPath, this.name)
}
data class Artist(var tracks: MutableList<Track> = mutableListOf(), var name: String = "<unknown>", var imgPath: String? = null) {
    fun toSkeleton() = SongCollectionSkeleton(this.imgPath, this.name)
}
@Serializable
data class SongCollectionSkeleton(val imgPath: String?, val name: String) {
    fun toPlaylist() = Playlist(imgPath = this.imgPath, name = this.name)
    fun toArtist() = Artist(imgPath = this.imgPath, name = this.name)
}

/*GLobal state components and global view model*/
data class FileInfo(
    val dataFile: File? = null,
    val dataReader: FileReader? = null,
    val dataWriter: FileWriter? = null
) {
    fun close() {
        this.dataReader?.close()
        this.dataWriter?.close()
    }
}

data class MainScreenState(val selected: MainScreenTabs = MainScreenTabs.PLAYLISTS)
enum class MainScreenTabs(val title: String, val index: Int) {
    PLAYLISTS("Playlists", 0), ALBUMS("Albums", 1), ARTISTS("Artists", 2)
}
data class GlobalState(
    val mainScreen: MainScreenState = MainScreenState(),
    val dataFile: File? = null,
    val trackList: MutableList<Track> = mutableListOf(),
    var selectedTrack: Track? = null,
    var contentResolver: ContentResolver? = null,
    val playlists: MutableMap<String, Playlist> = mutableMapOf(),
    val artists: MutableMap<String, Artist> = mutableMapOf(),
    val albums: MutableMap<String, Playlist> = mutableMapOf()
)
class GlobalViewModel : ViewModel() {
    private val internalState = MutableStateFlow(GlobalState())
    val publicState = this.internalState.asStateFlow()

    fun update(updater: (GlobalState) -> GlobalState) = this.internalState.update(updater)

    fun arrangeTracks(data: JsonContainer) {
        Log.d("FILE I/O", "Received data to arrange: $data.")
        val state = this.internalState.value
        val finalTrackList = mutableListOf<Track>()
        for (album in data.albums) {
            state.albums.put(album.name, album.toPlaylist())
        }
        for (artist in data.artists) {
            state.artists.put(artist.name, artist.toArtist())
        }
        for (playlist in data.playlists) {
            state.playlists.put(playlist.name, playlist.toPlaylist())
        }
        for (track in data.tracks) {
            val fullTrack = track.toTrack()
            finalTrackList.add(fullTrack)
            if (fullTrack.album != null && (fullTrack.album in state.albums)) {
                state.albums[fullTrack.album]?.tracks?.add(fullTrack)
            } else if (fullTrack.album != null) {
                state.albums.put(fullTrack.album!!, Playlist(mutableListOf(fullTrack), name = fullTrack.album!!))
            }
            for (pl in fullTrack.playlists) {
                if (pl in state.playlists) {
                    state.playlists[pl]?.tracks?.add(fullTrack)
                } else {
                    state.playlists.put(pl, Playlist(mutableListOf(fullTrack), name = pl))
                }
            }
            for (a in fullTrack.artists) {
                if (a in state.artists) {
                    state.artists[a]?.tracks?.add(fullTrack)
                } else {
                    state.artists.put(a, Artist(mutableListOf(fullTrack), name = a))
                }
            }
        }
        state.trackList.addAll(finalTrackList)
        this.cleanEmptyCollections()
    }

    fun cleanEmptyCollections() {
        val state = this.internalState.value
        val toDelete = mutableListOf<String>()
        for ((name, list) in state.playlists) {
            if (list.tracks.isEmpty()) {
                toDelete.add(name)
            }
        }
        for (name in toDelete) {
            state.playlists.remove(name)
        }
        toDelete.clear()
        for ((name, list) in state.artists) {
            if (list.tracks.isEmpty()) {
                toDelete.add(name)
            }
        }
        for (name in toDelete) {
            state.artists.remove(name)
        }
        toDelete.clear()
        for ((name, list) in state.albums) {
            if (list.tracks.isEmpty()) {
                toDelete.add(name)
            }
        }
        for (name in toDelete) {
            state.albums.remove(name)
        }
    }

    fun cleanInvalid() {
        val state = this.internalState.value
        val toDelete = mutableListOf<Track>()
        for (track in state.trackList) {
            val props = listOf(track::stdLyrics, track::trnLyrics, track::romLyrics)
            if (!File(track.path).exists()) {
                toDelete.add(track)
                continue
            }
            if (track.imgPath != null && !File(track.imgPath).exists()) {
                track.imgPath = null
            }
            for (prop in props) {
                if (prop.get()?.path != null && !File(prop.get()?.path).exists()) {
                    prop.set(null)
                }
            }
        }
        for (track in toDelete) {
            if (track.album != null) {
                state.albums[track.album]?.tracks?.remove(track)
            }
            for (pl in track.playlists) {
                state.playlists[pl]?.tracks?.remove(track)
            }
            for (art in track.artists) {
                state.artists[art]?.tracks?.remove(track)
            }
            state.trackList.remove(track)
        }
    }

    /**Retrieve the file paths for all audio present in the MediaStore. Still testing.*/
    fun getAllAudioPaths(cursor: Cursor? = null): List<String>? {
        val cr = this.internalState.value.contentResolver
        if (cr == null) {
            Log.w("MEDIASTORE QUERY", "No content resolver, aborting.")
            return null
        }
        val returnColumns = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.IS_MUSIC)
        val info = mutableListOf<String>()
        val mediaCursor = cursor ?: cr.query(
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            returnColumns,
            null,
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )
        if (mediaCursor == null) {
            Log.w("MEDIASTORE QUERY", "Cursor is null, aborting.")
            return null
        }
        Log.d("MEDIASTORE QUERY", "Amount of returned entries: ${mediaCursor.count}")
        mediaCursor.apply {
            if (!moveToFirst()) {
                Log.w("MEDIASTORE QUERY", "Cursor is empty, aborting.")
                return null
            }
            val colIndData = mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val colIndMusic = mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            while (moveToNext()) {
                if (true) { /*Replace true with "is music" condition when I find one that works.*/
                    info.add(mediaCursor.getString(colIndData).toString())
//                    Log.v("MEDIASTORE QUERY", "${mediaCursor.getString(colIndData)}: ${mediaCursor.getString(colIndMusic)}")
                }
            }
        }
        mediaCursor.close()
        return info
    }

    /**Check the MediaStore for files not present in the track list and add them with default parameters.*/
    fun checkForNewTracks() {
        /*I have absolutely zero idea hpw to go about this.*/
        /*Ref: https://stackoverflow.com/questions/6832522/playing-audio-from-mediastore-on-a-media-player-android*/
        val state = this.internalState.value
        val mediaList = MediaStore.Audio.Media()
        TODO()
    }

    /**Print all tracks, playlists, albums and artists in memory to Logcat.*/
    fun dump(level: (String, String) -> Unit = Log::v) {
        val state = this.internalState.value
        val tag = "GVM DUMP"
        var info = "Tracks:\n"
        for ((ind, track) in state.trackList.withIndex()) {
            info += "$ind: ${track.name} - ${track.artists}\n"
        }
        level(tag, info)
        info = "Albums:\n"
        for ((ind, album) in state.albums.values.toList().withIndex()) {
            info += "$ind: ${album.name}"
        }
        level(tag, info)
        info = "Playlists:\n"
        for ((ind, pl) in state.artists.values.toList().withIndex()) {
            info += "$ind: ${pl.name}"
        }
        level(tag, info)
        info = "Artists:\n"
        for ((ind, artist) in state.artists.values.toList().withIndex()) {
            info += "$ind: ${artist.name}"
        }
        level(tag, info)
    }
}

/*Routes and their supporting classes*/
class RouteList

@Serializable
data object MainScreenRoute

/*Others*/
@Serializable
data class JsonContainer(val tracks: List<TrackSkeleton>, val playlists: List<SongCollectionSkeleton>, val artists: List<SongCollectionSkeleton>, val albums: List<SongCollectionSkeleton>)