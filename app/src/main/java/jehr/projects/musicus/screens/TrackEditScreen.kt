package jehr.projects.musicus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jehr.projects.musicus.R
import jehr.projects.musicus.ui.theme.MusicusTheme
import jehr.projects.musicus.ui.theme.colourScheme
import jehr.projects.musicus.utils.TrackEditScreenRoute
import jehr.projects.musicus.utils.infoRepo

@Composable
fun TrackEditScreen(route: TrackEditScreenRoute) {
    MusicusTheme(dynamicColor = false) {
        Surface(modifier = Modifier
            .background(colourScheme.background)
            .fillMaxSize()
            .statusBarsPadding(), color = colourScheme.background) {
            Column {
                TrackEditTopBar()
                TrackEditBody()
                TrackEditBottomBar()
            }
        }
    }
}

@Composable
fun TrackEditTopBar() {
    Row {
        IconButton({ infoRepo.navController?.popBackStack() }) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
        }
        Text("Editing Details", style = MaterialTheme.typography.titleLarge, color = colourScheme.text)
    }
}

@Composable
fun TrackEditBody() {
    val tfvs = mutableMapOf("Key" to TextFieldValue())
    val paths = mutableMapOf("Key" to "Path", "Image Path" to "Coming Soon...")
    val aids = mapOf("Key" to mutableListOf("List", "of", "Options"))
    val fields = mapOf("Key" to "freetext/aided text/path", "Image Path" to "path")
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize(), color = colourScheme.foreground) {
        LazyColumn {
            item {
                Row(horizontalArrangement = Arrangement.Center) {
                    Image(painterResource(R.drawable.musicus_no_image), "Image")
                }
            }
            for ((key, type) in fields) {
                item {
                    when (type) {
                        "freetext" -> FreetextField(key, "Enter $key", tfvs, key)
                        "aided text" -> AidedTextField(key, "Enter $key", tfvs, key, aids[key] ?: mutableListOf("ERROR"))
                        "path" -> FileChooseField(key, paths, key)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackEditBottomBar() {

}

@Composable
fun FreetextField(label: String, placeholder: String, source: MutableMap<String, TextFieldValue>, key: String) {
    val tfv = source[key] ?: TextFieldValue("ERROR")
    Column {
        TextField(tfv, {source[key] = it}, label = { Text(label) }, placeholder = { Text(placeholder) })
        Spacer(Modifier.height(20.dp))
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
        Spacer(Modifier.height(20.dp))
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
            IconButton({TODO()}) {
                Icon(Icons.Default.Edit, "Choose file")
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}