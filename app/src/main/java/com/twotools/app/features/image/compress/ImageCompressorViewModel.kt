package com.twotools.app.features.image.compress

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

class ImageCompressorViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var originalDimensions by mutableStateOf<Pair<Int, Int>?>(null)
        private set

    var originalFileSize by mutableLongStateOf(0L)
        private set

    var qualitySlider by mutableFloatStateOf(65f)
        private set

    var compressedFile by mutableStateOf<File?>(null)
        private set

    var compressedFileSize by mutableLongStateOf(0L)
        private set

    var isCompressing by mutableStateOf(false)
        private set

    private var loadedBitmap: Bitmap? = null
    private var compressJob: Job? = null

    fun onImageSelected(context: Context, uri: Uri) {
        selectedImageUri = uri
        viewModelScope.launch {
            isCompressing = true
            originalFileSize = BitmapUtils.getFileSize(context, uri)
            originalDimensions = BitmapUtils.getDimensions(context, uri)

            loadedBitmap = BitmapUtils.decodeSampledBitmap(context, uri, maxDimension = 2560)
            isCompressing = false
            triggerCompression(context)
        }
    }

    fun onQualityChange(context: Context, quality: Float) {
        qualitySlider = quality
        triggerCompression(context)
    }

    private fun triggerCompression(context: Context) {
        val bitmap = loadedBitmap ?: return
        compressJob?.cancel()
        compressJob = viewModelScope.launch {
            delay(150) // debounce
            isCompressing = true
            val file = withContext(Dispatchers.IO) {
                BitmapUtils.saveBitmapToFile(
                    context = context,
                    bitmap = bitmap,
                    format = Bitmap.CompressFormat.JPEG,
                    quality = qualitySlider.toInt(),
                    prefix = "compressed"
                )
            }
            compressedFile = file
            compressedFileSize = file.length()
            isCompressing = false
        }
    }

    fun saveToDestination(destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        val file = compressedFile ?: return
        viewModelScope.launch {
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }

    fun shareCompressedFile() {
        val file = compressedFile ?: return
        storageManager.shareFile(file, "image/jpeg", "Share Compressed Image")
    }

    override fun onCleared() {
        super.onCleared()
        loadedBitmap?.recycle()
        loadedBitmap = null
    }
}
