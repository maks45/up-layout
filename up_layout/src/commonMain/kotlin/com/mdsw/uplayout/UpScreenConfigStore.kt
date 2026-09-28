package com.mdsw.uplayout

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.Json

const val DEFAULT_UP_SCREEN_CONFIG_FILE = "up_screen_config.json"

interface UpScreenConfigStore {
    suspend fun save(config: UpScreenConfig)
    suspend fun load(): UpScreenConfig
}

internal val upScreenConfigJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}

internal fun encodeConfig(config: UpScreenConfig): String =
    upScreenConfigJson.encodeToString(UpScreenConfig.serializer(), config)

internal fun decodeConfigOrEmpty(raw: String): UpScreenConfig =
    try {
        upScreenConfigJson.decodeFromString(UpScreenConfig.serializer(), raw)
    } catch (_: Exception) {
        UpScreenConfig()
    }

@Composable
expect fun rememberUpScreenConfigStore(
    fileName: String = DEFAULT_UP_SCREEN_CONFIG_FILE
): UpScreenConfigStore
