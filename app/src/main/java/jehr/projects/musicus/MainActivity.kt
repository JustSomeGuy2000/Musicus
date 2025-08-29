package jehr.projects.musicus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import jehr.projects.musicus.screens.ArtistScreen
import jehr.projects.musicus.screens.MainScreen
import jehr.projects.musicus.screens.PlayScreen
import jehr.projects.musicus.screens.PlaylistScreen
import jehr.projects.musicus.screens.TrackDataScreen
import jehr.projects.musicus.screens.TrackEditScreen
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.ui.theme.darkColourScheme
import jehr.projects.musicus.ui.theme.lightColourScheme
import jehr.projects.musicus.utils.ArtistScreenRoute
import jehr.projects.musicus.utils.DebugSettings
import jehr.projects.musicus.utils.GlobalViewModel
import jehr.projects.musicus.utils.JsonContainer
import jehr.projects.musicus.utils.MainScreenRoute
import jehr.projects.musicus.utils.PlayScreenRoute
import jehr.projects.musicus.utils.PlaylistScreenRoute
import jehr.projects.musicus.utils.TrackDataScreenRoute
import jehr.projects.musicus.utils.TrackEditScreenRoute
import jehr.projects.musicus.utils.debugLog
import jehr.projects.musicus.utils.exJSONContainer
import jehr.projects.musicus.utils.infoRepo
import jehr.projects.musicus.utils.musicRepo
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    lateinit var gvm: GlobalViewModel

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        infoRepo.setup(this)
        /*enableEdgeToEdge()*/
        setContent {
//            this.openFileOutput("data.json", Context.MODE_PRIVATE)
            val vmso = checkNotNull(LocalViewModelStoreOwner.current)
            GlobalVmViewer(vmso) {
                this.gvm = viewModel()
                NavWrapper()
            }
        }
    }

    override fun onResume() {
        infoRepo.setup(this)
        lifecycleScope.launch {
            try {
                musicRepo.arrangeTracks(Json.decodeFromString<JsonContainer>( infoRepo.dataFile!!.readText()))
            } catch(e: Exception) {
                debugLog("e", DebugSettings.MetadataRead, "Data read failed: $e.")
                debugLog("d", DebugSettings.MetadataRead, "Defaulting to example data...")
                musicRepo.arrangeTracks(exJSONContainer)
            }
            debugLog("d", DebugSettings.MediastoreQuery, "Result: ${musicRepo.getAllAudioPaths()}")
        }
        super.onResume()
    }

    override fun onPause() {
        val write = Json.encodeToString(musicRepo.toJsonContainer())
        infoRepo.dataFile?.writeText(write)
        debugLog("d", DebugSettings.FileIO, "Wrote to ${infoRepo.dataFile?.path} with content $write.")
        super.onPause()
    }
}

@Preview(showBackground = true, name = "Main Menu Light")
@Composable
fun NavWrapper() {
    colourScheme = if (isSystemInDarkTheme()) {
        darkColourScheme
    } else {
        lightColourScheme
    }
    val navController = rememberNavController()
    infoRepo.navController = navController
    NavHost(navController, startDestination = MainScreenRoute) {
        composable<MainScreenRoute> { entry -> MainScreen(entry.toRoute()) }
        composable<PlaylistScreenRoute> { entry -> PlaylistScreen(entry.toRoute()) }
        composable<ArtistScreenRoute> { entry -> ArtistScreen(entry.toRoute()) }
        composable<TrackDataScreenRoute> { entry -> TrackDataScreen(entry.toRoute()) }
        composable<TrackEditScreenRoute> { entry -> TrackEditScreen(entry.toRoute()) }
        composable<PlayScreenRoute> { entry -> PlayScreen(entry.toRoute()) }
    }
}

/**Bit of a hacky way to implement a global state. Within this, all `viewModel()` calls (should) result in the same one. */
@Composable
fun GlobalVmViewer(vmso: ViewModelStoreOwner, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { content() }
}