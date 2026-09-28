@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.mdsw.uplayout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

@Composable
actual fun rememberUpScreenConfigStore(fileName: String): UpScreenConfigStore {
    return remember(fileName) {
        FileUpScreenConfigStore(fileName)
    }
}

internal class FileUpScreenConfigStore(private val fileName: String) : UpScreenConfigStore {
    override suspend fun save(config: UpScreenConfig) {
        withContext(Dispatchers.Default) {
            NSString.create(string = encodeConfig(config))
                .writeToFile(configFilePath(), atomically = true, encoding = NSUTF8StringEncoding, error = null)
        }
    }

    override suspend fun load(): UpScreenConfig {
        return withContext(Dispatchers.Default) {
            val raw = NSString.stringWithContentsOfFile(configFilePath(), NSUTF8StringEncoding, null)
            if (raw == null) UpScreenConfig() else decodeConfigOrEmpty(raw)
        }
    }

    private fun configFilePath(): String {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )?.path
        requireNotNull(directory) { "UpScreenConfigStore: documents directory not found" }
        return "$directory/$fileName"
    }
}
