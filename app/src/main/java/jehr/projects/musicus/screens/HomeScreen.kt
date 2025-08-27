package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jehr.projects.musicus.R
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.ArtistScreenRoute
import jehr.projects.musicus.utils.GlobalViewModel
import jehr.projects.musicus.utils.MainScreenRoute
import jehr.projects.musicus.utils.MainScreenTabs
import jehr.projects.musicus.utils.Playlist
import jehr.projects.musicus.utils.PlaylistScreenRoute
import jehr.projects.musicus.utils.exJSONContainer
import jehr.projects.musicus.utils.infoRepo
import jehr.projects.musicus.utils.musicRepo

@Composable
fun MainScreen(startOn: MainScreenRoute) {
    MusicusTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .background(colourScheme.background)
                .fillMaxSize()
                .statusBarsPadding(), color = colourScheme.background
        ) {
            Column {
                TitleBar()
                NavRow(startOn)
                MainContent()
            }
        }
    }
}

@Composable
fun TitleBar() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Text(
                "Musicus",
                style = MaterialTheme.typography.titleMedium,
                color = colourScheme.text
            )
            Spacer(modifier = Modifier.width(160.dp))
            Text("Other stuff", color = colourScheme.text)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavRow(startOn: MainScreenRoute) {
    val navElements = MainScreenTabs.entries.toList()
    val gvm: GlobalViewModel = viewModel()
    val state = gvm.publicState.collectAsStateWithLifecycle().value.mainScreen
    SecondaryTabRow(state.selected.index, modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.secondary) {
        navElements.forEach { ele ->
            val isSelected = state.selected == ele
            Tab(isSelected, onClick = {
                gvm.update{ gs -> gs.copy(mainScreen = gs.mainScreen.copy(selected = ele))}}, modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        ele.title,
                        overflow = TextOverflow.Ellipsis,
                        color = colourScheme.text,
                        style = if (isSelected) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isSelected) {
                        Spacer(modifier = Modifier
                            .height(4.dp)
                            .fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val gvm: GlobalViewModel = viewModel()
    val state = gvm.publicState.collectAsStateWithLifecycle().value
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize()) {
        Column {
            Row {
                Text("Space for icon buttons", style = MaterialTheme.typography.bodyMedium, color = colourScheme.text)
            }
            when (state.mainScreen.selected) {
                MainScreenTabs.ALBUMS -> {
                    PlaylistGrid(musicRepo.albums) {
                        infoRepo.navController?.navigate(
                            PlaylistScreenRoute(it.value.name, MainScreenTabs.ALBUMS.index)
                        )
                    }
                }

                MainScreenTabs.PLAYLISTS -> {
                    PlaylistGrid(musicRepo.playlists) {
                        infoRepo.navController?.navigate(
                            PlaylistScreenRoute(
                                it.value.name,
                                MainScreenTabs.PLAYLISTS.index
                            )
                        )
                    }
                }

                MainScreenTabs.ARTISTS -> {
                    LazyColumn {
                        item {
                            Spacer(Modifier.height(8.dp))
                        }
                        musicRepo.artists.forEach {
                            item {
                                DisplayRow(
                                    R.drawable.musicus_no_image,
                                    it.value.name,
                                    "${it.value.tracks.size} ${if (it.value.tracks.size == 1) "track" else "tracks"}",
                                    {
                                        infoRepo.navController?.navigate(ArtistScreenRoute(it.key))
                                    })
                            }
                        }
                    }
                }

                MainScreenTabs.TRACKS -> {
                    LazyColumn {
                        item {
                            Spacer(Modifier.height(8.dp))
                        }
                        musicRepo.tracks.forEach {
                            item {
                                DisplayRow(
                                    R.drawable.musicus_no_image,
                                    it.name,
                                    it.artists.joinToString(", "),
                                    {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistGrid(playlists: MutableMap<String, Playlist>, nav: (Map.Entry<String, Playlist>) -> Unit) {
    LazyVerticalGrid(GridCells.Adaptive(180.dp)) {
        playlists.forEach {
            item {
                DisplayTile(R.drawable.musicus_no_image, it.value.name, "${it.value.primaryArtist ?: "<unknown>"} | ${it.value.tracks.size} ${if (it.value.tracks.size == 1) "track" else "tracks"}") { nav(it) }
            }
        }
    }
}

@Composable
fun DisplayTile(imgId: Int, name: String, author: String, nav: () -> Unit) {
    Box(Modifier
        .padding(8.dp)
        .clickable(onClick = nav), contentAlignment = Alignment.Center) {
        Column {
            Image(painterResource(imgId), "No desc", Modifier.clip(MaterialTheme.shapes.large))
            Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally), color = colourScheme.text)
            Text(author, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally), color = colourScheme.text)
        }
    }
}

@Composable
fun DisplayRow(imgId: Int, title: String, subtitle: String, onClick: () -> Unit, right: @Composable () -> Unit = {}) {
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)) {
            Spacer(Modifier.width(10.dp))
            Image(
                painterResource(imgId),
                "No desc",
                modifier = Modifier
                    .size(50.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.height(50.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colourScheme.text
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colourScheme.text
                )
            }
            right()
        }
        Spacer(Modifier.height(8.dp))
    }
}