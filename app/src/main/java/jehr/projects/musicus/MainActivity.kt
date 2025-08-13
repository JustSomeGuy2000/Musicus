package jehr.projects.musicus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : ComponentActivity() {
    lateinit var dataFile: File
    lateinit var gvm: GlobalViewModel

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*enableEdgeToEdge()*/
        setContent {
//            this.openFileOutput("data.json", Context.MODE_PRIVATE)
            this.dataFile = File(this.filesDir, "data.json")
            Log.d("FILE I/O", "Accessed metadata file ${dataFile.path}.")
            this.gvm = viewModel()
            gvm.update {gs -> gs.copy(dataFile = this.dataFile, contentResolver = this.contentResolver)}
            val coroScope = rememberCoroutineScope()
            val vmso = checkNotNull(LocalViewModelStoreOwner.current)
            GlobalVmViewer(vmso) {
                coroScope.launch {
                    try {
                        gvm.arrangeTracks(Json.decodeFromString<JsonContainer>( dataFile.readText()))
                    } catch(e: Exception) {
                        Log.e("METADATA READ", "Data read failed: $e.")
                        Log.d("METADATA READ", "Defaulting to example data...")
                        gvm.arrangeTracks(exJSONContainer)
                    }
                    Log.d("MEDIASTORE QUERY", "Result: ${gvm.getAllAudioPaths()}")
                }
                NavWrapper()
            }
        }
    }

    override fun onPause() {
        val state = this.gvm.publicState.value
        val write = Json.encodeToString(JsonContainer(state.trackList.map{it.toSkeleton()}, state.playlists.map{it.value.toSkeleton()}, state.artists.map{it.value.toSkeleton()}, state.albums.map{it.value.toSkeleton()}))
        this.dataFile.writeText(write)
        Log.d("FILE I/O", "Wrote to ${this.dataFile.path} with content $write.")
        super.onPause()
    }
}

@Preview(showBackground = true, name = "Main Menu Light")
@Composable
fun NavWrapper() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = MainScreenRoute) {
        composable<MainScreenRoute> { MainScreen() }
    }
}

@Composable
fun GlobalVmViewer(vmso: ViewModelStoreOwner, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { content() }
}