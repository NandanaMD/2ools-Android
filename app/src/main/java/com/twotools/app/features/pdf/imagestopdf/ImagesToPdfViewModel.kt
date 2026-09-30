package com.twotools.app.features.pdf.imagestopdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
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
import java.io.FileOutputStream
import kotlin.math.min

data class SelectedImage(
    val id: String,
    val uri: Uri,
    val rotation: Int = 0
)

class ImagesToPdfViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedImages by mutableStateOf<List<SelectedImage>>(emptyList())
        private set

    var generatedPdfFile by mutableStateOf<File?>(null)
        private set

    var generatedPdfSize by mutableLongStateOf(0L)
        private set

    var isGenerating by mutableStateOf(false)
        private set

    var progressPercent by mutableIntStateOf(0)
        private set

    var pageMarginPt by mutableIntStateOf(16) // None: 0, Small: 16, Normal: 32
        private set

    var isGrayscale by mutableStateOf(false)
        private set

    fun onImagesAdded(uris: List<Uri>) {
        val newItems = uris.map { uri ->
            SelectedImage(id = "${uri}_${System.nanoTime()}", uri = uri)
        }
        selectedImages = selectedImages + newItems
        generatedPdfFile = null
    }

    fun removeImage(index: Int) {
        if (index in selectedImages.indices) {
            selectedImages = selectedImages.toMutableList().apply { removeAt(index) }
            generatedPdfFile = null
        }
    }

    fun moveImageUp(index: Int) {
        if (index > 0 && index < selectedImages.size) {
            val list = selectedImages.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            selectedImages = list
            generatedPdfFile = null
        }
    }

    fun moveImageDown(index: Int) {
        if (index >= 0 && index < selectedImages.size - 1) {
            val list = selectedImages.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            selectedImages = list
            generatedPdfFile = null
        }
    }

    fun rotateImage(index: Int) {
        if (index in selectedImages.indices) {
            val list = selectedImages.toMutableList()
            val cur = list[index]
            list[index] = cur.copy(rotation = (cur.rotation + 90) % 360)
            selectedImages = list
            generatedPdfFile = null
        }
    }

    fun toggleGrayscale() {
        isGrayscale = !isGrayscale
        generatedPdfFile = null
    }

    fun setMargin(margin: Int) {
        pageMarginPt = margin
    }

    fun generatePdf(context: Context, onComplete: () -> Unit) {
        if (selectedImages.isEmpty()) return

        viewModelScope.launch {
            isGenerating = true
            progressPercent = 0

            val file = withContext(Dispatchers.IO) {
                val pdfDocument = PdfDocument()
                val pageWidth = 595 // Standard A4 width in pt
                val pageHeight = 842 // Standard A4 height in pt
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

                if (isGrayscale) {
                    val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
                    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                }

                try {
                    selectedImages.forEachIndexed { index, selectedItem ->
                        val baseBitmap = BitmapUtils.decodeSampledBitmap(
                            context = context,
                            uri = selectedItem.uri,
                            maxDimension = 2048
                        )

                        if (baseBitmap != null) {
                            val bitmap = if (selectedItem.rotation != 0) {
                                val matrix = Matrix().apply { postRotate(selectedItem.rotation.toFloat()) }
                                val rot = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.width, baseBitmap.height, matrix, true)
                                if (rot != baseBitmap) baseBitmap.recycle()
                                rot
                            } else baseBitmap

                            val isLandscape = bitmap.width > bitmap.height
                            val curPageWidth = if (isLandscape) pageHeight else pageWidth
                            val curPageHeight = if (isLandscape) pageWidth else pageHeight

                            val pageInfo = PdfDocument.PageInfo.Builder(curPageWidth, curPageHeight, index + 1).create()
                            val page = pdfDocument.startPage(pageInfo)
                            val canvas = page.canvas

                            canvas.drawColor(Color.WHITE)

                            val availWidth = curPageWidth - (pageMarginPt * 2)
                            val availHeight = curPageHeight - (pageMarginPt * 2)
                            val scale = min(availWidth.toFloat() / bitmap.width, availHeight.toFloat() / bitmap.height)
                            val destWidth = bitmap.width * scale
                            val destHeight = bitmap.height * scale
                            val left = pageMarginPt + (availWidth - destWidth) / 2f
                            val top = pageMarginPt + (availHeight - destHeight) / 2f

                            val destRect = RectF(left, top, left + destWidth, top + destHeight)
                            canvas.drawBitmap(bitmap, null, destRect, paint)

                            pdfDocument.finishPage(page)
                            bitmap.recycle()
                        }

                        progressPercent = (((index + 1).toFloat() / selectedImages.size) * 100).toInt()
                    }

                    val cacheDir = File(context.cacheDir, "twools_temp").apply { if (!exists()) mkdirs() }
                    val outputFile = File(cacheDir, "doc_${System.currentTimeMillis()}.pdf")
                    FileOutputStream(outputFile).use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    outputFile
                } catch (e: Exception) {
                    e.printStackTrace()
                    pdfDocument.close()
                    null
                }
            }

            generatedPdfFile = file
            generatedPdfSize = file?.length() ?: 0L
            isGenerating = false
            onComplete()
        }
    }

    fun saveToDestination(destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        val file = generatedPdfFile ?: return
        viewModelScope.launch {
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }

    fun sharePdf() {
        val file = generatedPdfFile ?: return
        storageManager.shareFile(file, "application/pdf", "Share PDF Document")
    }
}
