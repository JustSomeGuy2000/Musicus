package jehr.projects.musicus

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : ComponentActivity() {
    lateinit var dataFile: File
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
                Log.e("METADATA READ", "Data read failed: $e.")
                Log.d("METADATA READ", "Defaulting to example data...")
                musicRepo.arrangeTracks(exJSONContainer)
            }
            Log.d("MEDIASTORE QUERY", "Result: ${musicRepo.getAllAudioPaths()}")
        }
        super.onResume()
    }

    override fun onPause() {
        val write = Json.encodeToString(JsonContainer(musicRepo.tracks.map{it.toSkeleton()}, musicRepo.playlists.map{it.value.toSkeleton()}, musicRepo.artists.map{it.value.toSkeleton()}, musicRepo.albums.map{it.value.toSkeleton()}))
        this.dataFile.writeText(write)
        Log.d("FILE I/O", "Wrote to ${this.dataFile.path} with content $write.")
        super.onPause()
    }
}

@Preview(showBackground = true, name = "Main Menu Light")
@Composable
fun NavWrapper() {
    val navController = rememberNavController()
    infoRepo.navController = navController
    NavHost(navController, startDestination = MainScreenRoute()) {
        composable<MainScreenRoute> {entry -> MainScreen(entry.toRoute()) }
        composable<PlaylistScreenRoute> { entry -> PlaylistScreen(entry.toRoute()) }
    }
}

@Composable
fun GlobalVmViewer(vmso: ViewModelStoreOwner, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { content() }
}

//TODO: Implement a way to set the main screen state through the route (so returning from the artists or albums screen doesn't take you to the default playlists state)
//TODO: Register albums in artists and artists in playlists (albums and playlists).