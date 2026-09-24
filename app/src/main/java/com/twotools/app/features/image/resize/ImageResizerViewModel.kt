package com.twotools.app.features.image.resize

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twotools.app.core.storage.BitmapUtils
import com.twotools.app.core.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class ImageResizerViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var originalWidth by mutableIntStateOf(0)
        private set
    var originalHeight by mutableIntStateOf(0)
        private set
    var originalFileSize by mutableLongStateOf(0L)
        private set

    var targetWidth by mutableStateOf("0")
        private set
    var targetHeight by mutableStateOf("0")
        private set
    var isAspectRatioLocked by mutableStateOf(true)
        private set

    var selectedPercentage by mutableIntStateOf(50)
        private set

    var resizedFile by mutableStateOf<File?>(null)
        private set
    var resizedFileSize by mutableLongStateOf(0L)
        private set
    var isResizing by mutableStateOf(false)
        private set

    private var loadedBitmap: Bitmap? = null

    val estimatedFileSize: Long
        get() {
            val w = targetWidth.toLongOrNull() ?: 0L
            val h = targetHeight.toLongOrNull() ?: 0L
            if (originalWidth <= 0 || originalHeight <= 0 || w <= 0 || h <= 0) return 0L
            val ratio = (w * h).toDouble() / (originalWidth.toLong() * originalHeight.toLong()).toDouble()
            return (originalFileSize * ratio).toLong().coerceAtLeast(1024L)
        }

    fun onImageSelected(context: Context, uri: Uri) {
        selectedImageUri = uri
        resizedFile = null
        viewModelScope.launch {
            originalFileSize = BitmapUtils.getFileSize(context, uri)
            val dims = BitmapUtils.getDimensions(context, uri)
            originalWidth = dims.first
            originalHeight = dims.second

            val w = (originalWidth * 0.5).roundToInt().coerceAtLeast(1)
            val h = (originalHeight * 0.5).roundToInt().coerceAtLeast(1)
            targetWidth = w.toString()
            targetHeight = h.toString()

            loadedBitmap = BitmapUtils.decodeSampledBitmap(context, uri, maxDimension = 3000)
        }
    }

    fun onWidthChange(widthStr: String) {
        targetWidth = widthStr
        resizedFile = null
        val w = widthStr.toIntOrNull()
        if (w != null && isAspectRatioLocked && originalWidth > 0) {
            val ratio = originalHeight.toDouble() / originalWidth.toDouble()
            targetHeight = (w * ratio).roundToInt().toString()
        }
    }

    fun onHeightChange(heightStr: String) {
        targetHeight = heightStr
        resizedFile = null
        val h = heightStr.toIntOrNull()
        if (h != null && isAspectRatioLocked && originalHeight > 0) {
            val ratio = originalWidth.toDouble() / originalHeight.toDouble()
            targetWidth = (h * ratio).roundToInt().toString()
        }
    }

    fun onPercentageChange(percentage: Int) {
        selectedPercentage = percentage
        resizedFile = null
        if (originalWidth > 0 && originalHeight > 0) {
            val factor = percentage / 100.0
            targetWidth = (originalWidth * factor).roundToInt().coerceAtLeast(1).toString()
            targetHeight = (originalHeight * factor).roundToInt().coerceAtLeast(1).toString()
        }
    }

    fun toggleAspectRatioLock() {
        isAspectRatioLocked = !isAspectRatioLocked
    }

    fun applyResize(context: Context, onComplete: () -> Unit = {}) {
        val bitmap = loadedBitmap ?: return
        val w = targetWidth.toIntOrNull() ?: return
        val h = targetHeight.toIntOrNull() ?: return
        if (w <= 0 || h <= 0) return

        viewModelScope.launch {
            isResizing = true
            val file = withContext(Dispatchers.IO) {
                val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
                val f = BitmapUtils.saveBitmapToFile(
                    context = context,
                    bitmap = scaled,
                    format = Bitmap.CompressFormat.JPEG,
                    quality = 90,
                    prefix = "resized"
                )
                if (scaled != bitmap) {
                    scaled.recycle()
                }
                f
            }
            resizedFile = file
            resizedFileSize = file.length()
            isResizing = false
            onComplete()
        }
    }

    fun saveToDestination(context: Context, destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (resizedFile == null) {
                applyResize(context)
            }
            val file = resizedFile ?: return@launch
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }

    fun shareResizedFile(context: Context) {
        viewModelScope.launch {
            if (resizedFile == null) {
                applyResize(context)
            }
            val file = resizedFile ?: return@launch
            storageManager.shareFile(file, "image/jpeg", "Share Resized Image")
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadedBitmap?.recycle()
        loadedBitmap = null
    }
}
