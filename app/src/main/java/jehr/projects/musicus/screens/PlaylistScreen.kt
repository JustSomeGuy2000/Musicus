package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jehr.projects.musicus.R
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.GlobalViewModel
import jehr.projects.musicus.utils.Playlist
import jehr.projects.musicus.utils.PlaylistScreenRoute
import jehr.projects.musicus.utils.Track
import jehr.projects.musicus.utils.infoRepo
import jehr.projects.musicus.utils.musicRepo

@Composable
fun PlaylistScreen(playlist: PlaylistScreenRoute) {
    val pl = if (playlist.from == 0) {
        musicRepo.playlists[playlist.playlistName]!!
    } else {
        musicRepo.albums[playlist.playlistName]!!
    }
    val mui = remember{MutableInteractionSource()}
    val gvm: GlobalViewModel = viewModel()
    MusicusTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .background(colourScheme.background)
                .fillMaxSize()
                .statusBarsPadding()
                .clickable(mui, null){ gvm.update{gs -> gs.copy(playlistScreenState = gs.playlistScreenState.copy(editing = false))}}, color = colourScheme.background
        ) {
            Column {
                PlaylistHeader(pl)
                PlaylistBody(pl)
            }
        }
    }
}

@Composable
fun PlaylistHeader(playlist: Playlist) {
    val gvm: GlobalViewModel = viewModel()
    val editing = gvm.publicState.collectAsStateWithLifecycle().value.playlistScreenState.editing
    Box {
        Column(Modifier.fillMaxWidth()) {
            IconButton({ infoRepo.navController?.popBackStack()}) { Image(Icons.AutoMirrored.Filled.ArrowBack, "Back to main screen") }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.width(30.dp))
                Image(
                    painterResource(R.drawable.musicus_no_image),
                    "Playlist image",
                    Modifier.size(110.dp).clip(MaterialTheme.shapes.medium)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        playlist.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = colourScheme.text
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "${playlist.tracks.size} ${if (playlist.tracks.size == 1) "track" else "tracks"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colourScheme.text
                    )
                }
            }
            Spacer(Modifier.height(15.dp))
            if (editing) {
                val editingDesc = remember{mutableStateOf(playlist.desc ?: "")}
                Row {
                    Spacer(Modifier.width(30.dp))
                    TextField(
                        editingDesc.value,
                        { new -> playlist.desc = new; editingDesc.value = new },
                        placeholder = { Text("Enter description here") })
                }
            } else {
                if (playlist.desc == null || playlist.desc == "") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button({ gvm.update{gs -> gs.copy(playlistScreenState = gs.playlistScreenState.copy(editing = true))}}) {
                            Text(
                                "Add Description",
                                style = MaterialTheme.typography.bodySmall,
                                color = colourScheme.text
                            )
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(30.dp))
                        Text(
                            playlist.desc!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colourScheme.text
                        )
                        IconButton({ gvm.update{gs -> gs.copy(playlistScreenState = gs.playlistScreenState.copy(editing = true))}}) {
                            Icon(Icons.Default.Create, "Change description")
                        }
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable
fun PlaylistBody(playlist: Playlist) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            playlist.tracks.forEachIndexed { ind, track ->
                item {
                    TrackDisplayRow(ind, track)
                }
            }
        }
    }
}

@Composable
fun TrackDisplayRow(pos: Int, track: Track) {
    Column {
        Box(Modifier.fillMaxWidth(), Alignment.CenterStart) {
            Row {
                Spacer(Modifier.width(20.dp))
                Text(
                    "${pos + 1}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colourScheme.text
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    track.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colourScheme.text
                )
            }
            Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.End) {
                Text("${track.runtime}", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.bodyLarge, color = colourScheme.text)
                Spacer(Modifier.width(15.dp))
                TrackOptionsButton(track)
            }
        }
        HorizontalDivider()
    }
}