/*
 * Copyright (C) 2023 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.romainguy.kotlin.explorer

import androidx.compose.runtime.*
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPlacement.Floating
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readLines

private const val Optimize = "OPTIMIZE"
private const val Presentation = "PRESENTATION"
private const val ShowLineNumbers = "SHOW_LINE_NUMBERS"
private const val ShowDex = "SHOW_DEX"
private const val ShowOat = "SHOW_OAT"
private const val WindowPosX = "WINDOW_X"
private const val WindowPosY = "WINDOW_Y"
private const val WindowWidth = "WINDOW_WIDTH"
private const val WindowHeight = "WINDOW_HEIGHT"
private const val Placement = "WINDOW_PLACEMENT"

@Stable
class ExplorerState(
    val settings: Settings = Settings()
) {
    var toolPaths by mutableStateOf(createToolPaths(settings))
    var optimize by BooleanState(Optimize, true)
    var presentationMode by BooleanState(Presentation, false)
    var showLineNumbers by BooleanState(ShowLineNumbers, true)
    var showDex by BooleanState(ShowDex, true)
    var showOat by BooleanState(ShowOat, true)
    var sourceCode: String = readSourceCode(toolPaths)
    var windowWidth by IntState(WindowWidth, 1900)
    var windowHeight by IntState(WindowHeight, 1600)
    var windowPosX by IntState(WindowPosX, -1)
    var windowPosY by IntState(WindowPosY, -1)
    var windowPlacement by SettingsState(Placement, Floating) { WindowPlacement.valueOf(this) }

    fun reloadToolPathsFromSettings() {
        toolPaths = createToolPaths(settings)
    }

    private inner class BooleanState(key: String, initialValue: Boolean) :
        SettingsState<Boolean>(key, initialValue, { toBoolean() })

    private inner class IntState(key: String, initialValue: Int) :
        SettingsState<Int>(key, initialValue, { toInt() })

    private open inner class SettingsState<T>(private val key: String, initialValue: T, parse: String.() -> T) :
        MutableState<T> {
        private val state = mutableStateOf(settings.entries[key]?.parse() ?: initialValue)
        override var value: T
            get() = state.value
            set(value) {
                settings.entries[key] = value.toString()
                state.value = value
            }

        override fun component1() = state.component1()

        override fun component2() = state.component2()
    }
}

data class Settings(
    val directory: Path = settingsPath(),
    val file: Path = directory.resolve("settings"),
    val entries: MutableMap<String, String> = readSettings(file)
) {
    fun getValue(name: String, defaultValue: String) = entries[name] ?: defaultValue
}

private fun settingsPath() = Paths.get(System.getProperty("user.home"), ".kotlin-explorer").apply {
    if (!exists()) Files.createDirectory(this)
}

private fun readSettings(file: Path): MutableMap<String, String> {
    val settings = mutableMapOf<String, String>()
    if (!file.exists()) return settings

    val delimiter = Regex.fromLiteral("=")
    file.readLines().forEach { line ->
        val parts = line.split(delimiter, 2)
        if (parts.size == 2) {
            settings[parts[0]] = parts[1]
        }
    }
    return settings
}

private fun readSourceCode(toolPaths: ToolPaths) = if (toolPaths.sourceFile.exists()) {
    Files.readString(toolPaths.sourceFile)
} else {
    "fun square(a: Int): Int {\n    return a * a\n}\n"
}

fun writeState(state: ExplorerState) {
    Files.writeString(
        state.toolPaths.sourceFile,
        state.sourceCode
    )
    Files.writeString(
        state.settings.file,
        state.settings.entries
            .map { "${it.key}=${it.value}" }
            .joinToString("\n")
    )
}

