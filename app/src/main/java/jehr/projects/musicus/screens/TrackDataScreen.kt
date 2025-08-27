package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import jehr.projects.musicus.R
import jehr.projects.musicus.ui.theme.ColourScheme
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.Track
import jehr.projects.musicus.utils.TrackDataScreenRoute
import jehr.projects.musicus.utils.TrackEditScreenRoute
import jehr.projects.musicus.utils.infoRepo
import jehr.projects.musicus.utils.musicRepo

@Composable
fun TrackDataScreen(route: TrackDataScreenRoute) {
    val track = musicRepo.tracks[route.trackPos]
    MusicusTheme(dynamicColor = false) {
        Surface(modifier = Modifier
            .background(colourScheme.background)
            .fillMaxSize()
            .statusBarsPadding(), color = colourScheme.background) {
            Column {
                TrackDataTopBar(route.trackPos)
                TrackDataHeader(track)
                TrackDataBody(track)
            }
        }
    }
}

@Composable
fun TrackDataTopBar(pos: Int) {
    TwoSidedRow ({
        IconButton({ infoRepo.navController?.popBackStack() }) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
        }
    }) {
        IconButton({ infoRepo.navController?.navigate(TrackEditScreenRoute(pos)) }) {
            Icon(Icons.Default.Edit, "Edit track details")
        }
    }
}

@Composable
fun TrackDataHeader(track: Track) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.width(15.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.width(30.dp))
            Image(
                painterResource(R.drawable.musicus_no_image),
                "Playlist image",
                Modifier
                    .size(110.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    track.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colourScheme.text
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    track.artists.joinToString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colourScheme.text
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Row {
            Spacer(Modifier.width(30.dp))
            if (track.desc != null) {
                Text(track.desc!!, style = MaterialTheme.typography.bodyMedium, color = colourScheme.text)
            }
        }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
fun TrackDataBody(track: Track) {
    val fields = mapOf("Title" to track.name, "Original Name" to (track.originalName ?: "None"), "Artist" to track.artists.joinToString(), "Language" to track.lang.joinToString(), "Original Language" to track.originalLang.joinToString(), "Origin" to track.origin, "Origin Info" to (track.originInfo ?: "None"), "Runtime" to track.runtime.toString(), "Cover" to (if (track.cover) "Yes" else "No"), "Album" to (track.album ?: "None"), "Date Added" to track.dateAdded.toString(), "Times Played" to track.timesPlayed.toString(), "Standard Lyrics File" to (track.stdLyrics?.path ?: "None"), "Romanised Lyrics File" to (track.romLyrics?.path ?: "None"), "Translated Lyrics File" to (track.trnLyrics?.path ?: "None"))
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize(), color = colourScheme.foreground) {
        LazyColumn {
            for ((title, content) in fields) {
                item {
                    InfoRow(title, content)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, content: String) {
    Column {
        Row {
            Spacer(Modifier.width(10.dp))
            Column {
                Spacer(Modifier.height(15.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colourScheme.label
                )
                Text(
                    content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colourScheme.text
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        HorizontalDivider()
    }
}

@Composable
fun TwoSidedRow(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth(), Alignment.CenterStart) {
        Row (verticalAlignment = Alignment.CenterVertically) {left()}
        Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {right()}
    }
}