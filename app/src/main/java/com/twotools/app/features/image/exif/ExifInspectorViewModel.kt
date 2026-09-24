package com.twotools.app.features.image.exif

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twotools.app.core.storage.BitmapUtils
import com.twotools.app.core.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

data class ExifMetadata(
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val dateTime: String? = null,
    val iso: String? = null,
    val fNumber: String? = null,
    val exposureTime: String? = null,
    val focalLength: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val width: Int? = null,
    val height: Int? = null
) {
    val hasLocation: Boolean get() = latitude != null && longitude != null
    val hasCameraInfo: Boolean get() = !cameraMake.isNullOrBlank() || !cameraModel.isNullOrBlank()
}

class ExifInspectorViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var metadata by mutableStateOf<ExifMetadata?>(null)
        private set

    var strippedFile by mutableStateOf<File?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    fun onImageSelected(context: Context, uri: Uri) {
        selectedImageUri = uri
        strippedFile = null
        viewModelScope.launch {
            isProcessing = true
            metadata = readExif(context, uri)
            isProcessing = false
        }
    }

    private suspend fun readExif(context: Context, uri: Uri): ExifMetadata = withContext(Dispatchers.IO) {
        try {
            val stream: InputStream? = context.contentResolver.openInputStream(uri)
            if (stream == null) return@withContext ExifMetadata()
            val exif = ExifInterface(stream)
            stream.close()

            val latLong = FloatArray(2)
            val hasGps = exif.getLatLong(latLong)

            ExifMetadata(
                cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE),
                cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL),
                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME),
                iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY),
                fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" },
                exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { "${it}s" },
                focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "${it}mm" },
                latitude = if (hasGps) latLong[0].toDouble() else null,
                longitude = if (hasGps) latLong[1].toDouble() else null,
                altitude = if (hasGps) exif.getAltitude(0.0) else null,
                width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).takeIf { it > 0 },
                height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).takeIf { it > 0 }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExifMetadata()
        }
    }

    fun stripMetadata(context: Context, onComplete: () -> Unit) {
        val uri = selectedImageUri ?: return
        viewModelScope.launch {
            isProcessing = true
            val file = withContext(Dispatchers.IO) {
                val bitmap = BitmapUtils.decodeSampledBitmap(context, uri, maxDimension = 3000)
                    ?: return@withContext null
                val cleanFile = BitmapUtils.saveBitmapToFile(
                    context = context,
                    bitmap = bitmap,
                    format = Bitmap.CompressFormat.JPEG,
                    quality = 95,
                    prefix = "clean"
                )
                bitmap.recycle()
                cleanFile
            }
            strippedFile = file
            isProcessing = false
            onComplete()
        }
    }

    fun saveCleanFileToDestination(destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        val file = strippedFile ?: return
        viewModelScope.launch {
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }

    fun shareCleanFile() {
        val file = strippedFile ?: return
        storageManager.shareFile(file, "image/jpeg", "Share Sanitized Image")
    }
}
