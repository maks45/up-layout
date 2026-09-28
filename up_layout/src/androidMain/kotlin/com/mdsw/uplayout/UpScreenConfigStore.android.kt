package com.mdsw.uplayout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberUpScreenConfigStore(fileName: String): UpScreenConfigStore {
    val context = LocalContext.current
    return remember(fileName) {
        FileUpScreenConfigStore(File(context.filesDir, fileName))
    }
}

internal class FileUpScreenConfigStore(private val file: File) : UpScreenConfigStore {
    override suspend fun save(config: UpScreenConfig) {
        withContext(Dispatchers.IO) {
            file.writeText(encodeConfig(config))
        }
    }

    override suspend fun load(): UpScreenConfig {
        return withContext(Dispatchers.IO) {
            if (!file.exists()) {
                UpScreenConfig()
            } else {
                decodeConfigOrEmpty(file.readText())
            }
        }
    }
}
