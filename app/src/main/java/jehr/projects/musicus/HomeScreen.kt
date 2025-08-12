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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jehr.projects.musicus.ui.theme.MusicusTheme

@Composable
fun MainScreen() {
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
                    state.albums.forEach {
                        item(it.value) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(painterResource(R.drawable.musicus_no_image), "Album image")
                                Text(it.value.name, style = MaterialTheme.typography.bodyMedium)
                                Text(it.value.primaryArtist ?: "<unknown>", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            MainScreenTabs.PLAYLISTS -> {
                LazyVerticalGrid(GridCells.Adaptive(180.dp)) {
                    state.playlists.forEach {
                        item(it.value) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(painterResource(R.drawable.musicus_no_image), "Playlist image")
                                Text(it.value.name, style = MaterialTheme.typography.bodyMedium)
                                Text(it.value.primaryArtist ?: "<unknown>", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            MainScreenTabs.ARTISTS -> {
                LazyColumn {
                    state.artists.forEach {
                        item {
                            Row {
                                Image(painterResource(R.drawable.musicus_no_image), "Artist image")
                                Column {
                                    Text(it.value.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(it.value.tracks.size.toString(), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}