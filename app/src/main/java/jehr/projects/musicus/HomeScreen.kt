package jehr.projects.musicus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import jehr.projects.musicus.ui.theme.MusicusTheme

@Composable
fun MainScreen(startOn: MainScreenRoute) {
    MusicusTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .fillMaxSize()
                .statusBarsPadding(), color = MaterialTheme.colorScheme.secondary
        ) {
            Column {
                TitleBar()
                NavRow()
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
                color = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(modifier = Modifier.width(160.dp))
            Text("Other stuff", color = MaterialTheme.colorScheme.inversePrimary)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavRow() {
    val navElements = MainScreenTabs.entries.toList()
    val gvm: GlobalViewModel = viewModel()
    val state = gvm.publicState.collectAsStateWithLifecycle().value.mainScreen
    val selected = state.selected
    SecondaryTabRow(selected.index, modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.secondary) {
        navElements.forEach { ele ->
            Tab(selected == ele, onClick = {gvm.update{gs -> gs.copy(mainScreen = gs.mainScreen.copy(selected = ele))}}, modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        ele.title,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        style = if (selected == ele) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (selected == ele) {
                        Spacer(modifier = Modifier.height(4.dp).fillMaxWidth())
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
        when (state.mainScreen.selected) {
            MainScreenTabs.ALBUMS -> {
                LazyVerticalGrid(GridCells.Adaptive(180.dp)) {
                    musicRepo.albums.forEach {
                        item {
                            DisplayTile(R.drawable.musicus_no_image, it.value.name, "${it.value.primaryArtist ?: "<unknown>"} | ${it.value.tracks.size} ${if (it.value.tracks.size == 1) "track" else "tracks"}") {
                                infoRepo.navController?.navigate(
                                    PlaylistScreenRoute(it.value.name, MainScreenTabs.ALBUMS)
                                )
                            }
                        }
                    }
                }
            }
            MainScreenTabs.PLAYLISTS -> {
                LazyVerticalGrid(GridCells.Adaptive(180.dp)) {
                    musicRepo.playlists.forEach {
                        item {
                            DisplayTile(R.drawable.musicus_no_image, it.value.name, "${it.value.primaryArtist ?: "<unknown>"} | ${it.value.tracks.size} ${if (it.value.tracks.size == 1) "track" else "tracks"}", {
                                infoRepo.navController?.navigate(
                                    PlaylistScreenRoute(it.value.name, MainScreenTabs.PLAYLISTS)
                                )
                            })
                        }
                    }
                }
            }
            MainScreenTabs.ARTISTS -> {
                LazyColumn {
                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                    musicRepo.artists.forEach {
                        item {
                            ArtistDisplayRow(R.drawable.musicus_no_image, it.value.name, it.value.tracks.size.toString(), ::TODO)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayTile(imgId: Int, name: String, author: String, nav: () -> Unit) {
    Box(Modifier.padding(8.dp).clickable(onClick = nav), contentAlignment = Alignment.Center) {
        Column {
            Image(painterResource(imgId), "No desc", Modifier.clip(MaterialTheme.shapes.large))
            Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.inversePrimary)
            Text(author, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.inversePrimary)
        }
    }
}

@Composable
fun ArtistDisplayRow(imgId: Int, name: String, size: String, nav: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = nav)) {
            Image(
                painterResource(imgId),
                "No desc",
                modifier = Modifier.size(50.dp).clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.height(50.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inversePrimary
                )
                Text(
                    "$size ${if (size == "1") "track" else "tracks"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}