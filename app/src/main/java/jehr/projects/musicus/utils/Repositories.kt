package jehr.projects.musicus.utils

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.navigation.NavHostController
import jehr.projects.musicus.MainActivity
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.contains
import kotlin.collections.get
import kotlin.collections.iterator

class MusicRepo(val tracks: MutableList<Track> = mutableListOf(),
                val playlists: MutableMap<String, Playlist> = mutableMapOf(),
                val artists: MutableMap<String, Artist> = mutableMapOf(),
                val albums: MutableMap<String, Playlist> = mutableMapOf()) {
    fun arrangeTracks(data: JsonContainer) {
        Log.d("FILE I/O", "Received data to arrange: $data.")
        val finalTrackList = mutableListOf<Track>()
        for (artist in data.artists) {
            this.artists.put(artist.name, artist.toArtist())
        }
        for (album in data.albums) {
            this.albums.put(album.name, album.toPlaylist())
        }
        for (playlist in data.playlists) {
            this.playlists.put(playlist.name, playlist.toPlaylist())
        }
        for (track in data.tracks) { //Populate playlists, albums and the general tracks list with tracks.
            val fullTrack = track.toTrack()
            finalTrackList.add(fullTrack)
            if (fullTrack.album != null) {
                if (fullTrack.album in this.albums) {
                    this.albums[fullTrack.album]?.tracks?.add(fullTrack)
                } else {
                    this.albums.put(
                        fullTrack.album!!,
                        Playlist(mutableListOf(fullTrack), name = fullTrack.album!!)
                    )
                }
            }
            for (pl in fullTrack.playlists) {
                if (pl in this.playlists) {
                    this.playlists[pl]?.tracks?.add(fullTrack)
                } else {
                    this.playlists.put(pl, Playlist(mutableListOf(fullTrack), name = pl))
                }
            }
            for (a in fullTrack.artists) {
                if (a in this.artists) {
                    this.artists[a]?.tracks?.add(fullTrack)
                } else {
                    this.artists.put(a, Artist(mutableListOf(fullTrack), name = a))
                }
            }
        }
        this.tracks.addAll(finalTrackList)
        for (pl in this.playlists.values) pl.sortArtists() //Populate playlists with artists
        for (album in this.albums.values) { //Populate albums with artists and artists with albums
            album.sortArtists()
            album.addToArtists()
        }
        this.cleanEmptyCollections()
    }

    fun cleanEmptyCollections() {
        val toDelete = mutableListOf<String>()
        for ((name, list) in this.playlists) {
            if (list.tracks.isEmpty()) {
                toDelete.add(name)
            }
        }
        for (name in toDelete) {
            this.playlists.remove(name)
        }
        toDelete.clear()
        for ((name, list) in this.artists) {
            if (list.tracks.isEmpty()) {
                toDelete.add(name)
            }
        }
        for (name in toDelete) {
            this.artists.remove(name)
        }
        toDelete.clear()
        for ((name, list) in this.albums) {
            if (list.tracks.isEmpty()) {
                toDelete.add(name)
            }
        }
        for (name in toDelete) {
            this.albums.remove(name)
        }
    }

    fun cleanInvalidPaths() {
        val toDelete = mutableListOf<Track>()
        for (track in this.tracks) {
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
                this.albums[track.album]?.tracks?.remove(track)
            }
            for (pl in track.playlists) {
                this.playlists[pl]?.tracks?.remove(track)
            }
            for (art in track.artists) {
                this.artists[art]?.tracks?.remove(track)
            }
            this.tracks.remove(track)
        }
    }

    /**Print all tracks, playlists, albums and artists in memory to Logcat.*/
    fun dump(level: (String, String) -> Unit = Log::v) {
        val tag = "GVM DUMP"
        var info = "Tracks:\n"
        for ((ind, track) in this.tracks.withIndex()) {
            info += "$ind: ${track.name} - ${track.artists}\n"
        }
        level(tag, info)
        info = "Albums:\n"
        for ((ind, album) in this.albums.values.toList().withIndex()) {
            info += "$ind: ${album.name}\n"
        }
        level(tag, info)
        info = "Playlists:\n"
        for ((ind, pl) in this.artists.values.toList().withIndex()) {
            info += "$ind: ${pl.name}\n"
        }
        level(tag, info)
        info = "Artists:\n"
        for ((ind, artist) in this.artists.values.toList().withIndex()) {
            info += "$ind: ${artist.name}\n"
        }
        level(tag, info)
    }

    /**Retrieve the file paths for all audio present in the MediaStore. Still testing.*/
    fun getAllAudioPaths(cursor: Cursor? = null): List<String>? {
        val cr = infoRepo.contentResolver
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
        val mediaList = MediaStore.Audio.Media()
        TODO()
    }

    /**Remove duplicate entries from the track list. Not sure why they would crop up in the first place, but they do.*/
    fun deduplicate() {
        val toRemove = mutableListOf<Track>()
        for (track in this.tracks) {
            Log.d("DEDUPLICATION", "Track: $track")
            Log.d("DEDUPLICATION", "First encounter: ${this.tracks.indexOf(track)}")
            Log.d("DEDUPLICATION", "Last encounter: ${this.tracks.lastIndexOf(track)}")
            if (this.tracks.indexOf(track) != this.tracks.lastIndexOf(track) && track !in toRemove) {
                toRemove.add(track)
            }
        }
        for (track in toRemove) {
            Log.d("DEDUPLICATION", "Removing track $track")
            track.delete()
        }
    }
}

val musicRepo = MusicRepo()

class InfoRepo(var contentResolver: ContentResolver? = null,
               var dataFile: File? = null,
               var navController: NavHostController? = null) {

    fun setup(ctx: MainActivity, force: Boolean = false) {
        if (this.dataFile == null || force) {
            this.dataFile = File(ctx.filesDir, "data.json")
            if (!this.dataFile!!.exists()) {
                this.dataFile!!.createNewFile()
            }
            Log.d("FILE I/O", "Accessed metadata file ${dataFile!!.path}.")
        }
        if (this.contentResolver == null || force) {
            this.contentResolver = ctx.contentResolver
        }
    }
}

val infoRepo = InfoRepo()