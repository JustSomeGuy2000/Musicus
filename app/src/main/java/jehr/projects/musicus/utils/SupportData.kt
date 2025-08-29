package jehr.projects.musicus.utils

import android.graphics.BitmapFactory
import androidx.annotation.IntRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlin.text.iterator

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

    fun toMs(): Int =
        this.ms + (this.seconds * 1000) + (this.minutes * 60000) + (this.hours * 360000)

    fun toStringWithMs(): String = "${this}${if (this.ms != 0) "." + this.ms.toString() else ""}"

    companion object {
        fun breakDownMs(ms: Int): List<Int> {
            val millis = ms % 1000
            var sec = (ms - (ms % 1000)) / 1000.0
            var min = (sec - (sec % 60)) / 60.0
            sec -= min * 60
            val hrs = (min - (min % 60)) / 60.0
            min -= hrs * 60
            return listOf(hrs.toInt(), min.toInt(), sec.toInt(), millis.toInt())
        }
    }

    fun decrement(ms: Int) {
        val time = breakDownMs(ms)
        this.hours -= time[0]
        this.minutes -= time[1]
        this.seconds -= time[2]
        this.ms -= time[3]
    }

    fun increment(ms: Int) = this.decrement(-ms)
}

@Serializable
data class MusicusDate(val day: Int, val month: Int, val year: Int) {
    override fun toString(): String {
        return "$day/$month/$year"
    }
}

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
    var file: File? = null,
    var desc: String? = null
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

    /**Remove this track from every collection that includes it, effectively erasing it from the app.*/
    fun delete() {
        for (art in this.artists) {
            musicRepo.artists[art]?.tracks?.remove(this)
        }
        for (pl in this.playlists) {
            musicRepo.playlists[pl]?.tracks?.remove(this)
        }
        if (this.album != null) {
            musicRepo.albums[this.album]?.tracks?.remove(this)
        }
        musicRepo.tracks.remove(this)
        this.close()
    }

    fun toSkeleton() = TrackSkeleton(this.path, this.name, this.originalName, this.lang, this.origin, this.originInfo, this.runtime, this.cover, this.originalLang, this.album, this.playlists, this.artists, this.imgPath, this.dateAdded, this.timesPlayed, this.stdLyrics?.toSkeleton(), this.trnLyrics?.toSkeleton(), this.romLyrics?.toSkeleton(), this.selectedLyrics, this.desc)

    /**Take in a map in a predetermined format and manually extract information from it.*/
    fun importFromMap(source: Map<String, String>) {
        this.imgPath = source["Image Path"] ?: this.imgPath
        this.name = source["Name"] ?: this.name
        this.originalName = source["Original Name"] ?: this.originalName
        this.desc = source["Description"] ?: this.desc
        if (source.containsKey("Artists")) {
            for (artist in this.artists) {
                musicRepo.artists[artist]?.tracks?.remove(this)
            }
            this.artists.clear()
            val extractedArtists = split(source["Artists"] ?: "")
            this.artists.addAll(extractedArtists)
            for (artist in extractedArtists) {
                if (!musicRepo.artists.containsKey(artist)) {
                    musicRepo.artists.put(artist, Artist(mutableListOf(this), artist))
                } else {
                    musicRepo.artists[artist]?.tracks?.add(this)
                }
            }
        }
        if (source.containsKey("Language")) {
            this.lang = split(source["Language"] ?: "").toMutableList()
            for (lang in this.lang) {
                langList.createIfNotFound(lang)
            }
        }
        if (source.containsKey("Original Language")) {
            this.originalLang = split(source["Original Language"] ?: "").toMutableList()
            for (lang in this.originalLang) {
                langList.createIfNotFound(lang)
            }
        }
        this.origin = source["Origin"] ?: this.origin
        this.originInfo = source["Origin Info"] ?: this.originInfo
        this.cover = source["Cover"]?.toBoolean() ?: this.cover
        this.album = source["Album"] ?: this.album
        if (this.album != null && !musicRepo.albums.containsKey(this.album)) {
            musicRepo.albums.put(this.album!!, Playlist(mutableListOf(this), name = this.album!!))
            musicRepo.albums[this.album]?.sortArtists()
        }
        if (source.containsKey("Original Lyrics") && source["Original Lyrics"] != "None") {
            this.stdLyrics = Lyrics(source["Original Lyrics"]!!)
        } else {
            this.stdLyrics = null
        }
        if (source.containsKey("Romanised Lyrics") && source["Romanised Lyrics"] != "None") {
            this.romLyrics = Lyrics(source["Romanised Lyrics"]!!)
        } else {
            this.romLyrics = null
        }
        if (source.containsKey("Translated Lyrics") && source["Translated Lyrics"] != "None") {
            this.trnLyrics = Lyrics(source["Translated Lyrics"]!!)
        } else {
            this.trnLyrics = null
        }
        musicRepo.cleanEmptyCollections()
    }
}
@Serializable
data class TrackSkeleton(val path: String, val name: String, val originalName: String?, val lang: MutableList<String>, val origin: String, val originInfo: String?, val runtime: Duration, val cover: Boolean, val originalLang: MutableList<String>, val album: String?, val playlists: MutableList<String>, val artists: MutableList<String>, val imgPath: String?, val dateAdded: MusicusDate?, val timesPlayed: Int, val stdLyrics: LyricsSkeleton?, val trnLyrics: LyricsSkeleton?, val romLyrics: LyricsSkeleton?, val selectedLyrics: Int, val desc: String?) {

    fun toTrack() = Track(path = this.path, name = this.name, originalName = this.originalName, lang = this.lang, origin = this.origin, originInfo = this.originInfo, runtime = this.runtime, cover = this.cover, originalLang = this.originalLang, album = this.album, playlists = this.playlists, artists = this.artists, imgPath = this.imgPath, dateAdded = this.dateAdded, timesPlayed = this.timesPlayed, stdLyrics = this.stdLyrics?.toLyrics(), trnLyrics = this.trnLyrics?.toLyrics(), romLyrics = this.romLyrics?.toLyrics(), selectedLyrics = this.selectedLyrics, file = null, desc = this.desc
    )
}

open class SongCollection(val tracks: MutableList<Track> = mutableListOf(), var imgPath: String? = null, var name: String = "<unknown>", var desc: String? = null) {
    fun sumDuration(): Duration {
        val time = Duration()
        for (track in this.tracks) {
            time.increment(track.runtime.toMs())
        }
        return time
    }
}
class Playlist(tracks: MutableList<Track> = mutableListOf(), imgPath: String? = null, var primaryArtist: String? = null, name: String = "<unknown>", val artists: MutableMap<Artist, Int> = mutableMapOf(), desc: String? = null): SongCollection(tracks, imgPath, name, desc) {
    fun toSkeleton() = SongCollectionSkeleton(this.imgPath, this.name, this.desc)

    /**Populate the artist list with all artists contained within this collection. Automatically sets a primary artist.*/
    fun sortArtists() {
        for (track in this.tracks) {
            for (artist in track.artists) {
                val trueArtist = musicRepo.artists[artist]!!
                if (trueArtist !in this.artists) {
                    this.artists.put(musicRepo.artists[artist]!!, 1)
                } else {
                    this.artists[trueArtist] = this.artists[trueArtist]!! + 1
                }
            }
        }
        this.inferPrimaryArtist()
    }

    /**Use the information from the artist list to set the primary artist. It is decided as the most prolific artist in this playlist. Ties are broken by first one encountered.*/
    fun inferPrimaryArtist() {
        var artistCount: Pair<MutableList<String>, Int> = Pair(mutableListOf(), 0)
        for ((artist, count) in this.artists) {
            if (count > artistCount.second) {
                artistCount = Pair(mutableListOf(artist.name), count)
            } else if (count == artistCount.second) {
                artistCount.first.add(artist.name)
            }
        }
        this.primaryArtist = artistCount.first[0]
    }

    /**Add this album to the album lists of all relevant artists.*/
    fun addToArtists() {
        for (artist in this.artists.keys) {
            if (this.name !in artist.albums.keys) {
                artist.albums.put(this.name, this)
            }
        }
    }
}
class Artist(tracks: MutableList<Track> = mutableListOf(), name: String = "<unknown>", imgPath: String? = null, val albums: MutableMap<String, Playlist> = mutableMapOf(), desc: String? = null): SongCollection(tracks, imgPath, name, desc) {
    fun toSkeleton() = SongCollectionSkeleton(this.imgPath, this.name, this.desc)
}
@Serializable
data class SongCollectionSkeleton(val imgPath: String?, val name: String, val desc: String?) {
    fun toPlaylist() = Playlist(imgPath = this.imgPath, name = this.name, desc = this.desc)
    fun toArtist() = Artist(name = this.name, imgPath = this.imgPath, desc = this.desc)
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
data class ArtistScreenState(var selected: Int = 0, var editing: Boolean = false)
data class PlaylistScreenState(var editing: Boolean = false)
enum class MainScreenTabs(val title: String, val index: Int) {
    PLAYLISTS("Playlists", 0), ALBUMS("Albums", 1), ARTISTS("Artists", 2), TRACKS("Tracks", 3)
}
data class GlobalState(
    val mainScreen: MainScreenState = MainScreenState(),
    val artistScreenState: ArtistScreenState = ArtistScreenState(),
    val playlistScreenState: PlaylistScreenState = PlaylistScreenState(),
    var selectedTrack: Track? = null,
)
class GlobalViewModel : ViewModel() {
    private val internalState = MutableStateFlow(GlobalState())
    val publicState = this.internalState.asStateFlow()

    fun update(updater: (GlobalState) -> GlobalState) = this.internalState.update(updater)
}

/*Routes and their supporting classes*/
class RouteList

@Serializable
data object MainScreenRoute
@Serializable
data class PlaylistScreenRoute(val playlistName: String, @param:IntRange(0, 1) val from: Int)
@Serializable
data class ArtistScreenRoute(val artistName: String)
@Serializable
data class TrackDataScreenRoute(val trackPos: Int)
@Serializable
data class TrackEditScreenRoute(val trackPos: Int)
@Serializable
data class PlayScreenRoute(val trackPos: Int)

/*Others*/
@Serializable
data class JsonContainer(val tracks: List<TrackSkeleton>, val playlists: List<SongCollectionSkeleton>, val artists: List<SongCollectionSkeleton>, val albums: List<SongCollectionSkeleton>)

enum class FieldTypes(val id: String) {
    FREETEXT("freetext"), AIDEDTEXT("aided text"), PATH("path"), BOOLEAN("boolean"), LEGEND("freeText/aidText/path/bool")
}

/**Split a string into a list of substrings according to a delimiter, optionally taking into account escapes and strpping whitespace.*/
fun split(source: String, delimiter: String = ",", escape: String? = "\\", strip: Boolean = true): List<String> {
    val result = mutableListOf<String>()
    var buffer = ""
    var ignore = false
    var pointer = 0
    for (char in source) {
        buffer += char
        pointer += 1
        val consider = buffer.substring(buffer.length-delimiter.length, buffer.length)
        if (consider == delimiter && !ignore) {
            buffer = buffer.substring(0, buffer.length-delimiter.length)
            if (strip) {
                buffer = buffer.trim()
            }
            result.add(buffer)
            buffer = ""
            ignore = false
        } else if (pointer == source.length) {
            if (strip) {
                buffer = buffer.trim()
            }
            result.add(buffer)
        } else if (escape != null && consider == escape) {
            if (ignore) {
                buffer += escape
                ignore = false
            } else {
                ignore = true
            }
        } else if (ignore) {
            ignore = false
        }
    }
    return result.toList()
}