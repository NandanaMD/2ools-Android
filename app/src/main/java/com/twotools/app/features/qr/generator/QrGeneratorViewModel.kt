package com.twotools.app.features.qr.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twotools.app.core.storage.BitmapUtils
import com.twotools.app.core.storage.StorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class QrType(val label: String) {
    URL("Website URL"),
    TEXT("Plain Text"),
    WIFI("Wi-Fi Network")
}

data class ColorOption(val name: String, val color: Int)

class QrGeneratorViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedType by mutableStateOf(QrType.URL)
        private set

    // URL fields
    var urlInput by mutableStateOf("https://")
        private set

    // Text fields
    var textInput by mutableStateOf("")
        private set

    // Wi-Fi fields
    var wifiSsid by mutableStateOf("")
        private set
    var wifiPassword by mutableStateOf("")
        private set
    var wifiSecurity by mutableStateOf("WPA/WPA2")
        private set
    var wifiHidden by mutableStateOf(false)
        private set

    // Styling
    val colorOptions = listOf(
        ColorOption("Classic Black", Color.BLACK),
        ColorOption("Deep Navy", Color.rgb(15, 32, 67)),
        ColorOption("Royal Purple", Color.rgb(63, 14, 82)),
        ColorOption("Emerald Green", Color.rgb(12, 77, 43)),
        ColorOption("Crimson Red", Color.rgb(117, 19, 19))
    )
    var selectedColorIndex by mutableIntStateOf(0)
        private set

    // Result State
    var qrBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var lastSavedFile by mutableStateOf<File?>(null)
        private set

    private var generateJob: Job? = null

    init {
        triggerGeneration()
    }

    fun onTypeChange(type: QrType) {
        selectedType = type
        triggerGeneration()
    }

    fun onUrlChange(value: String) {
        urlInput = value
        triggerGeneration()
    }

    fun onTextChange(value: String) {
        textInput = value
        triggerGeneration()
    }

    fun onWifiSsidChange(value: String) {
        wifiSsid = value
        triggerGeneration()
    }

    fun onWifiPasswordChange(value: String) {
        wifiPassword = value
        triggerGeneration()
    }

    fun onWifiSecurityChange(value: String) {
        wifiSecurity = value
        triggerGeneration()
    }

    fun onWifiHiddenChange(value: Boolean) {
        wifiHidden = value
        triggerGeneration()
    }

    fun onColorChange(index: Int) {
        selectedColorIndex = index
        triggerGeneration()
    }

    private fun getCurrentPayload(): String {
        return when (selectedType) {
            QrType.URL -> urlInput.trim()
            QrType.TEXT -> textInput.trim()
            QrType.WIFI -> {
                if (wifiSsid.isBlank()) ""
                else QrCodeEngine.buildWifiPayload(wifiSsid, wifiPassword, wifiSecurity, wifiHidden)
            }
        }
    }

    private fun triggerGeneration() {
        generateJob?.cancel()
        generateJob = viewModelScope.launch {
            delay(150) // Debounce rapid keystrokes
            val payload = getCurrentPayload()
            if (payload.isBlank()) {
                qrBitmap = null
                return@launch
            }
            isGenerating = true
            val fgColor = colorOptions[selectedColorIndex].color
            qrBitmap = QrCodeEngine.generateQrBitmap(
                content = payload,
                foregroundColor = fgColor,
                sizePx = 800
            )
            isGenerating = false
        }
    }

    fun shareQrCode(context: Context) {
        val bitmap = qrBitmap ?: return
        viewModelScope.launch {
            val file = BitmapUtils.saveBitmapToFile(
                context = context,
                bitmap = bitmap,
                format = Bitmap.CompressFormat.PNG,
                prefix = "qr_code"
            )
            lastSavedFile = file
            storageManager.shareFile(file, "image/png", "Share QR Code")
        }
    }

    fun saveQrCodeToDestination(context: Context, destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        val bitmap = qrBitmap ?: return
        viewModelScope.launch {
            val file = BitmapUtils.saveBitmapToFile(
                context = context,
                bitmap = bitmap,
                format = Bitmap.CompressFormat.PNG,
                prefix = "qr_code"
            )
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }
}
