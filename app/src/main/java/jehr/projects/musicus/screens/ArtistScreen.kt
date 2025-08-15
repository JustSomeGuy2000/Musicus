package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jehr.projects.musicus.ui.theme.MusicusTheme
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
    MusicusTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .fillMaxSize()
                .statusBarsPadding(), color = MaterialTheme.colorScheme.secondary
        ) {
            Column {
                ArtistHeader(artist.name)
                ArtistNavBar(artist)
                ArtistBodyTracks(artist)
            }
        }
    }
}

@Composable
fun ArtistHeader(name: String) {
    Row {
        IconButton({ infoRepo.navController?.popBackStack()}) {
            Image(Icons.AutoMirrored.Filled.ArrowBack, "Back to main screen")
        }
        Text(name, Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.inversePrimary)
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
                        TrackDisplayRow(ind, track.name, track.runtime)
                    }
                }
            }
        } else {
            PlaylistGrid(artist.albums) { infoRepo.navController?.navigate(
                PlaylistScreenRoute(it.value.name, MainScreenTabs.ALBUMS.index)) }
        }
    }
}