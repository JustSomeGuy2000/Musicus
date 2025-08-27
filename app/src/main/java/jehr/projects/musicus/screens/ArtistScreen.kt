package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.Artist
import jehr.projects.musicus.utils.ArtistScreenRoute
import jehr.projects.musicus.utils.GlobalViewModel
import jehr.projects.musicus.utils.MainScreenTabs
import jehr.projects.musicus.utils.PlaylistScreenRoute
import jehr.projects.musicus.utils.infoRepo
import jehr.projects.musicus.utils.musicRepo

@Composable
fun ArtistScreen(route: ArtistScreenRoute) {
    val artist = musicRepo.artists[route.artistName] ?: Artist()
    val gvm: GlobalViewModel = viewModel()
    MusicusTheme(dynamicColor = false) {
        val mis = remember{ MutableInteractionSource() }
        Surface(
            modifier = Modifier
                .background(colourScheme.background)
                .fillMaxSize()
                .statusBarsPadding()
                .clickable(mis, null) {gvm.update{gs -> gs.copy(artistScreenState = gs.artistScreenState.copy(editing = false))}},
                color = colourScheme.background
        ) {
            Column {
                ArtistHeader(artist)
                ArtistNavBar(artist)
                ArtistBodyTracks(artist)
            }
        }
    }
}

@Composable
fun ArtistHeader(artist: Artist) {
    val gvm: GlobalViewModel = viewModel()
    val editing = gvm.publicState.collectAsStateWithLifecycle().value.artistScreenState.editing
    Column(Modifier.fillMaxWidth()) {
        Row {
            IconButton({gvm.update{gs -> gs.copy(artistScreenState = gs.artistScreenState.copy(editing = false))}; infoRepo.navController?.popBackStack()}) {
                Image(Icons.AutoMirrored.Filled.ArrowBack, "Back to main screen")
            }
            Text(
                artist.name,
                Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.titleLarge,
                color = colourScheme.text
            )
        }
        if (editing) {
            val editingDesc = remember{mutableStateOf(artist.desc ?: "")}
            TextField(editingDesc.value, {new -> artist.desc = new; editingDesc.value = new}, placeholder = {Text("Enter description here")})
        } else {
            if (artist.desc != null && artist.desc != "") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(15.dp))
                    Text(
                        artist.desc!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colourScheme.text
                    )
                    IconButton({ gvm.update{gs -> gs.copy(artistScreenState = gs.artistScreenState.copy(editing = true))}}) {
                        Icon(Icons.Default.Create, "Change description")
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button({ gvm.update{gs -> gs.copy(artistScreenState = gs.artistScreenState.copy(editing = true))}}) {
                        Text(
                            "Add Description",
                            style = MaterialTheme.typography.bodySmall,
                            color = colourScheme.text
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistNavBar(artist: Artist) {
    val gvm: GlobalViewModel = viewModel()
    val state = gvm.publicState.collectAsStateWithLifecycle().value.artistScreenState
    val colour = MaterialTheme.colorScheme
    val buttonColors = ButtonColors(colour.secondary, colour.inversePrimary, colour.secondary, colour.inversePrimary)
    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
        Button({ gvm.update {gs -> gs.copy(artistScreenState = gs.artistScreenState.copy(selected = 0))} }, colors = buttonColors) { Text("Tracks (${artist.tracks.size})", style = if (state.selected == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.inversePrimary) }
        Spacer(Modifier.width(15.dp))
        Button({ gvm.update {gs -> gs.copy(artistScreenState = gs.artistScreenState.copy(selected = 1))} }, colors = buttonColors) { Text("Albums (${artist.albums.size})", style = if (state.selected == 1) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.inversePrimary) }
    }
}

@Composable
fun ArtistBodyTracks(artist: Artist) {
    val gvm: GlobalViewModel = viewModel()
    val state = gvm.publicState.collectAsStateWithLifecycle().value.artistScreenState
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize()) {
        if (state.selected == 0) {
            LazyColumn {
                artist.tracks.forEachIndexed { ind, track ->
                    item {
                        TrackDisplayRow(ind, track)
                    }
                }
            }
        } else {
            PlaylistGrid(artist.albums) { infoRepo.navController?.navigate(
                PlaylistScreenRoute(it.value.name, MainScreenTabs.ALBUMS.index)) }
        }
    }
}