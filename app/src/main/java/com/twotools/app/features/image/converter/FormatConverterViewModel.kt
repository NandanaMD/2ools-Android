package com.twotools.app.features.image.converter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twotools.app.core.storage.BitmapUtils
import com.twotools.app.core.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ImageOutputFormat(val label: String, val extension: String, val mimeType: String, val description: String) {
    JPEG("JPEG", ".jpg", "image/jpeg", "Universal photo format with efficient compression"),
    PNG("PNG", ".png", "image/png", "Lossless format preserving crisp lines and transparency"),
    WEBP("WebP", ".webp", "image/webp", "Modern format combining high quality with minimal size")
}

class FormatConverterViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var originalFileSize by mutableLongStateOf(0L)
        private set

    var targetFormat by mutableStateOf(ImageOutputFormat.WEBP)
        private set

    var qualitySlider by mutableFloatStateOf(85f)
        private set

    var convertedFile by mutableStateOf<File?>(null)
        private set
    var convertedFileSize by mutableLongStateOf(0L)
        private set
    var isConverting by mutableStateOf(false)
        private set

    private var loadedBitmap: Bitmap? = null
    private var convertJob: Job? = null

    fun onImageSelected(context: Context, uri: Uri) {
        selectedImageUri = uri
        viewModelScope.launch {
            isConverting = true
            originalFileSize = BitmapUtils.getFileSize(context, uri)
            loadedBitmap = BitmapUtils.decodeSampledBitmap(context, uri, maxDimension = 3000)
            isConverting = false
            triggerConversion(context)
        }
    }

    fun onFormatChange(context: Context, format: ImageOutputFormat) {
        targetFormat = format
        triggerConversion(context)
    }

    fun onQualityChange(context: Context, quality: Float) {
        qualitySlider = quality
        triggerConversion(context)
    }

    private fun triggerConversion(context: Context) {
        val bitmap = loadedBitmap ?: return
        convertJob?.cancel()
        convertJob = viewModelScope.launch {
            delay(150)
            isConverting = true
            val file = withContext(Dispatchers.IO) {
                val compressFormat = when (targetFormat) {
                    ImageOutputFormat.PNG -> Bitmap.CompressFormat.PNG
                    ImageOutputFormat.WEBP -> {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            Bitmap.CompressFormat.WEBP_LOSSY
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                    }
                    ImageOutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
                }

                BitmapUtils.saveBitmapToFile(
                    context = context,
                    bitmap = bitmap,
                    format = compressFormat,
                    quality = qualitySlider.toInt(),
                    prefix = "converted"
                )
            }
            convertedFile = file
            convertedFileSize = file.length()
            isConverting = false
        }
    }

    fun saveToDestination(destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        val file = convertedFile ?: return
        viewModelScope.launch {
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }

    fun shareConvertedFile() {
        val file = convertedFile ?: return
        storageManager.shareFile(file, targetFormat.mimeType, "Share Converted Image")
    }

    override fun onCleared() {
        super.onCleared()
        loadedBitmap?.recycle()
        loadedBitmap = null
    }
}
