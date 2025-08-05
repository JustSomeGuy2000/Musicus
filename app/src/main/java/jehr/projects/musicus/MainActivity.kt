package jehr.projects.musicus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jehr.projects.musicus.ui.theme.MusicusTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

data class GlobalState(val mainScreen: MainScreenState = MainScreenState())
data class MainScreenState(val selected: Int = 1)
class GlobalViewModel: ViewModel() {
    private val internalState = MutableStateFlow(GlobalState())
    val publicState = this.internalState.asStateFlow()

    fun update(updater: (GlobalState) -> GlobalState) = this.internalState.update(updater)
}
@Serializable
data object MainScreenRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*enableEdgeToEdge()*/
        setContent {
            MusicusTheme {
                NavWrapper()
            }
        }
    }
}

class RouteList
@Preview(showBackground = true, name = "Main Menu Light")
@Composable
fun NavWrapper() {
    val navController = rememberNavController()
    val vmStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    NavHost(navController, startDestination = MainScreenRoute) {
        composable<MainScreenRoute> { GlobalVmViewer(vmStoreOwner)
            { MainScreen() }
        }
    }
}

@Composable
fun GlobalVmViewer(vmso: ViewModelStoreOwner, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { content() }
}
@Composable
fun MainScreen() {
    MusicusTheme(dynamicColor = false) {
        Surface(modifier = Modifier
            .background(MaterialTheme.colorScheme.secondary)
            .fillMaxSize()
            .statusBarsPadding(), color = MaterialTheme.colorScheme.secondary) {
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
    val navElements = listOf("Playlists", "Albums", "Artists")
    val gvm: GlobalViewModel = viewModel()
    val state = gvm.publicState.collectAsStateWithLifecycle().value.mainScreen
    val selected = state.selected
    SecondaryTabRow(selected, modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.secondary) {
        navElements.forEachIndexed { ind, ele ->
            Tab(selected == ind, onClick = {gvm.update{gs -> gs.copy(mainScreen = gs.mainScreen.copy(selected = ind))}}, modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        ele,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        style = if (selected == ind) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (selected == ind) {
                        Spacer(modifier = Modifier.height(4.dp).fillMaxWidth().background(Color(0xFF2196F3)))
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Placeholder")
        }
    }
}