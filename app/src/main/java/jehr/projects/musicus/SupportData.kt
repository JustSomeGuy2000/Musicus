package jehr.projects.musicus

import android.graphics.BitmapFactory
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

    fun toMs(): Int =
        this.ms + (this.seconds * 1000) + (this.minutes * 60000) + (this.hours * 360000)

    fun toStringWithMs(): String = "${this}${if (this.ms != 0) "." + this.ms.toString() else ""}"

    companion object {
        fun breakDownMs(ms: Int): List<Int> {
            val millis = ms % 1000
            var sec = (ms - (ms % 1000)) / 1000
            var min = (sec - (sec % 60)) / 60
            sec -= min * 60
            val hrs = (min - (min % 60)) / 60
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

open class SongCollection(val tracks: MutableList<Track> = mutableListOf(), var imgPath: String? = null, var name: String = "<unknown>") {
    fun sumDuration(): Duration {
        val time = Duration()
        for (track in this.tracks) {
            time.increment(track.runtime.toMs())
        }
        return time
    }
}
class Playlist(tracks: MutableList<Track> = mutableListOf(), imgPath: String? = null, var primaryArtist: String? = null, name: String = "<unknown>", val artists: MutableMap<String, Artist> = mutableMapOf()): SongCollection(tracks, imgPath, name) {
    fun toSkeleton() = SongCollectionSkeleton(this.imgPath, this.name)
}
class Artist(tracks: MutableList<Track> = mutableListOf(), name: String = "<unknown>", imgPath: String? = null, val albums: MutableMap<String, Playlist> = mutableMapOf(), ): SongCollection(tracks, imgPath, name) {
    fun toSkeleton() = SongCollectionSkeleton(this.imgPath, this.name)
}
@Serializable
data class SongCollectionSkeleton(val imgPath: String?, val name: String) {
    fun toPlaylist() = Playlist(imgPath = this.imgPath, name = this.name)
    fun toArtist() = Artist(name = this.name, imgPath = this.imgPath,)
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
data class MainScreenRoute(val selected: Int? = null)
@Serializable
data class PlaylistScreenRoute(val playlistName: String, val from: MainScreenTabs)

/*Others*/
@Serializable
data class JsonContainer(val tracks: List<TrackSkeleton>, val playlists: List<SongCollectionSkeleton>, val artists: List<SongCollectionSkeleton>, val albums: List<SongCollectionSkeleton>)