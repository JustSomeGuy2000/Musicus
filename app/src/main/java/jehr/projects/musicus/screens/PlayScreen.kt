package jehr.projects.musicus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.PlayScreenRoute
import jehr.projects.musicus.utils.musicRepo

@Composable
fun PlayScreen(route: PlayScreenRoute) {
    val track = musicRepo.tracks[route.trackPos]
    MusicusTheme(dynamicColor = false) {
        Surface(modifier = Modifier
            .background(colourScheme.background)
            .fillMaxSize()
            .statusBarsPadding(), color = colourScheme.background) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PlayScreenTopBar()
                PlayScreenMid()
                PlayScreenBottomBars()
            }
        }
    }
}

@Composable
fun PlayScreenTopBar() {

}

@Composable
fun PlayScreenMid() {

}

@Composable
fun PlayScreenBottomBars() {

}
