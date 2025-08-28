package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jehr.projects.musicus.R
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.black
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.DebugSettings
import jehr.projects.musicus.utils.FieldTypes
import jehr.projects.musicus.utils.Track
import jehr.projects.musicus.utils.TrackEditScreenRoute
import jehr.projects.musicus.utils.debugLog
import jehr.projects.musicus.utils.infoRepo
import jehr.projects.musicus.utils.langList
import jehr.projects.musicus.utils.musicRepo
import kotlinx.coroutines.launch

@Composable
fun TrackEditScreen(route: TrackEditScreenRoute) {
    val track = musicRepo.tracks[route.trackPos]
    val freeText = FieldTypes.FREETEXT
    val aidText = FieldTypes.AIDEDTEXT
    val path = FieldTypes.PATH
    val bool = FieldTypes.BOOLEAN
    val tfvs = remember{mutableStateMapOf("Key" to TextFieldValue(), "Name" to TextFieldValue(track.name), "Original Name" to TextFieldValue(track.originalName ?: ""), "Language" to TextFieldValue(track.lang.joinToString()), "Origin" to TextFieldValue(track.origin), "Origin Info" to TextFieldValue(track.originInfo ?: ""), "Original Language" to TextFieldValue(track.originalLang.joinToString()), "Album" to TextFieldValue(track.album ?: "None"), "Artists" to TextFieldValue(track.artists.joinToString()), "Description" to TextFieldValue(track.desc ?: "None"))}
    val paths = remember{mutableStateMapOf("Key" to "Path", "Image Path" to "Coming Soon...", "Original Lyrics" to (track.stdLyrics?.path ?: "None"), "Romanised Lyrics" to (track.romLyrics?.path ?: "None"), "Translated Lyrics" to (track.trnLyrics?.path ?: "None"))}
    val bools = remember{mutableStateMapOf("Key" to true, "Cover" to track.cover)}
    val aids = mapOf("Key" to mutableListOf("List", "of", "Options"), "Language" to langList.langList, "Original Language" to langList.langList, "Album" to musicRepo.albums.keys.toMutableList(), "Artists" to musicRepo.artists.keys.toMutableList())
    val fields = mapOf("Key" to FieldTypes.LEGEND, "Image Path" to path, "Name" to freeText, "Original Name" to freeText, "Description" to freeText, "Artists" to aidText, "Language" to aidText, "Original Language" to aidText,"Origin" to freeText, "Origin Info" to freeText, "Cover" to bool, "Album" to aidText, "Original Lyrics" to path, "Romanised Lyrics" to path, "Translated Lyrics" to path)
    MusicusTheme(dynamicColor = false) {
        Surface(modifier = Modifier
            .background(colourScheme.background)
            .fillMaxSize()
            .statusBarsPadding(), color = colourScheme.background) {
            Column {
                TrackEditTopBar()
                TrackEditBody(tfvs, paths, bools, aids, fields, track)
                //TrackEditBottomBar(tfvs, paths, bools, track)
            }
        }
    }
}

@Composable
fun TrackEditTopBar() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton({ infoRepo.navController?.popBackStack() }) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
        }
        Text("Editing Details", style = MaterialTheme.typography.titleLarge, color = colourScheme.text)
    }
}

@Composable
fun TrackEditBody(tfvs: SnapshotStateMap<String, TextFieldValue>, paths: SnapshotStateMap<String, String>, bools: SnapshotStateMap<String, Boolean>, aids: Map<String, MutableList<String>>, fields: Map<String, FieldTypes>, track: Track) {
    Column {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxSize().weight(1f, false),
            color = colourScheme.foreground
        ) {
            LazyColumn {
                item {
                    Column {
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Image(
                                painterResource(R.drawable.musicus_no_image),
                                "Image",
                                Modifier.size(100.dp).clip(MaterialTheme.shapes.medium)
                            )
                        }
                    }
                }
                for ((key, type) in fields) {
                    item {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            Row {
                                Spacer(Modifier.width(15.dp))
                                when (type) {
                                    FieldTypes.FREETEXT -> FreetextField(key, "Enter $key", tfvs, key)
                                    FieldTypes.AIDEDTEXT -> AidedTextField(
                                        key,
                                        "Enter $key",
                                        tfvs,
                                        key,
                                        aids[key] ?: mutableListOf("ERROR")
                                    )
                                    FieldTypes.PATH -> FileChooseField(key, paths, key)
                                    FieldTypes.BOOLEAN -> BooleanField(key, bools, key)
                                    else -> {}
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        TrackEditBottomBar(tfvs, paths, bools, track)
    }
}

@Composable
fun TrackEditBottomBar(tfvs: SnapshotStateMap<String, TextFieldValue>, paths: SnapshotStateMap<String, String>, bools: SnapshotStateMap<String, Boolean>, track: Track) {
    val coroScope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextButton({
            coroScope.launch {
                val info = mutableMapOf<String, String>()
                info.putAll(tfvs.map { Pair(it.key, it.value.text) })
                info.putAll(paths)
                info.putAll(bools.map { Pair(it.key, it.toString()) })
                track.importFromMap(info.toMap())
                infoRepo.navController?.popBackStack()
            }}, Modifier.weight(0.5f, true)) {Text("Save", style = MaterialTheme.typography.titleLarge, color = colourScheme.text)}
        TextButton({infoRepo.navController?.popBackStack()}, Modifier.weight(0.5f, true)) {Text("Cancel", style = MaterialTheme.typography.titleLarge, color = colourScheme.text)}
    }
}

@Composable
fun FreetextField(label: String, placeholder: String, source: MutableMap<String, TextFieldValue>, key: String) {
    Column {
        TextField(source[key] ?: TextFieldValue("ERROR"), {source[key] = it}, label = { Text(label) }, placeholder = { Text(placeholder) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AidedTextField(label: String, placeholder: String, valueSource: MutableMap<String, TextFieldValue>, key: String, helpSource: MutableList<String>) {
    val tfv = valueSource[key] ?: TextFieldValue("ERROR")
    val expanded = remember{ mutableStateOf(false) }
    Column {
        ExposedDropdownMenuBox(expanded.value, {expanded.value = it}) {
            TextField(
                tfv,
                { valueSource[key] = it },
                Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                label = { Text(label) },
                placeholder = { Text(placeholder) })
            ExposedDropdownMenu(expanded.value, {expanded.value = false}) {
                for (help in helpSource) {
                    DropdownMenuItem({Text(help, style = MaterialTheme.typography.bodyMedium, color = colourScheme.text)}, {valueSource[key] = TextFieldValue(help); expanded.value = false})
                }
            }
        }
    }
}

@Composable
fun FileChooseField(label: String, source: MutableMap<String, String>, key: String) {
    val text = source[key] ?: "ERROR"
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colourScheme.text)
        Spacer(Modifier.height(5.dp))
        TwoSidedRow({
            Text(text, style = MaterialTheme.typography.bodySmall, color = colourScheme.text, overflow = TextOverflow.Ellipsis)
        }) {
            IconButton({ debugLog("d", DebugSettings.RANDOM, "Pressed!") }) {
                Icon(Icons.Default.Edit, "Choose file", tint = black)
            }
        }
    }
}

@Composable
fun BooleanField(label: String, source: SnapshotStateMap<String, Boolean>, key: String) {
    TwoSidedRow({Text(label, style = MaterialTheme.typography.bodyLarge, color = colourScheme.text)}) {
        Switch(source[key] ?: false, {source[key] = it})
    }
}