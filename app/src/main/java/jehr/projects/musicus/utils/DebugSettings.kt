package jehr.projects.musicus.utils

import android.util.Log

enum class DebugSettings(val tag: String, val enabled: Boolean) {
    MetadataRead("METADATA READ", true), MediastoreQuery("MEDIASTORE QUERY", true), FileIO("FILE I/O", true), GvmDump("GVM DUMP", true), Deduplication("DEDUPLICATION", true), RANDOM("RANDOM", true)
}

fun debugLog(level: String, tag: DebugSettings, message: String, force: Boolean = false) {
    val func: (String, String) -> Unit = when (level) {
        "v" -> Log::v
        "i" -> Log::i
        "w" -> Log::w
        "e" -> Log::e
        else -> Log::d
    }
    if (tag.enabled || force) {
        func(tag.tag, message)
    }
}

fun debugLog(level: (String, String) -> Unit, tag: DebugSettings, message: String, force: Boolean = false) {
    if (tag.enabled || force) {
        level(tag.tag, message)
    }
}