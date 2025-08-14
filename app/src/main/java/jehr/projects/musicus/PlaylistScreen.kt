package jehr.projects.musicus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jehr.projects.musicus.ui.theme.MusicusTheme

@Composable
fun PlaylistScreen(playlist: PlaylistScreenRoute) {
    var from: Int
    var pl: Playlist? = null
    if (playlist.from == MainScreenTabs.PLAYLISTS) {
        pl = musicRepo.playlists[playlist.playlistName]!!
        from = 0
    } else {
        pl = musicRepo.albums[playlist.playlistName]!!
        from = 1
    }
    MusicusTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .fillMaxSize()
                .statusBarsPadding(), color = MaterialTheme.colorScheme.secondary
        ) {
            Column {
                PlaylistHeader(pl, from)
                PlaylistBody(pl)
            }
        }
    }
}

@Composable
fun PlaylistHeader(playlist: Playlist, from: Int) {
    Box {
        IconButton({infoRepo.navController?.navigate(MainScreenRoute(from))}) { Image(Icons.AutoMirrored.Filled.ArrowBack, "Back to main screen") }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource(R.drawable.musicus_no_image),
                "Playlist image",
                Modifier.size(180.dp).clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "${playlist.tracks.size} ${if (playlist.tracks.size == 1) "track" else "tracks"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun PlaylistBody(playlist: Playlist) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            playlist.tracks.forEachIndexed { ind, track ->
                item {
                    TrackDisplayRow(ind, track.name)
                }
            }
        }
    }
}

@Composable
fun TrackDisplayRow(pos: Int, name: String) {
    Column {
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(20.dp))
            Text(
                "$pos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(Modifier.width(20.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.inversePrimary
            )
        }
        Spacer(Modifier.height(5.dp))
        HorizontalDivider()
    }
}