package com.twotools.app.features.pdf.pdftoimages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

data class ExtractedPage(
    val pageIndex: Int,
    val file: File
)

class PdfToImagesViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    var selectedPdfUri by mutableStateOf<Uri?>(null)
        private set

    var pageCount by mutableIntStateOf(0)
        private set

    var isExtracting by mutableStateOf(false)
        private set

    var extractedPages by mutableStateOf<List<ExtractedPage>>(emptyList())
        private set

    var outputFormat by mutableStateOf(Bitmap.CompressFormat.JPEG)
        private set

    fun onPdfSelected(context: Context, uri: Uri) {
        selectedPdfUri = uri
        extractedPages = emptyList()
        viewModelScope.launch {
            isExtracting = true
            val count = withContext(Dispatchers.IO) {
                try {
                    val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
                    if (pfd != null) {
                        val renderer = PdfRenderer(pfd)
                        val total = renderer.pageCount
                        renderer.close()
                        pfd.close()
                        total
                    } else 0
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
            pageCount = count
            isExtracting = false
            if (count > 0) {
                extractAllPages(context)
            }
        }
    }

    fun setFormat(format: Bitmap.CompressFormat) {
        outputFormat = format
    }

    fun extractAllPages(context: Context) {
        val uri = selectedPdfUri ?: return
        viewModelScope.launch {
            isExtracting = true
            val pages = withContext(Dispatchers.IO) {
                val results = mutableListOf<ExtractedPage>()
                try {
                    val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
                    if (pfd != null) {
                        val renderer = PdfRenderer(pfd)
                        for (i in 0 until renderer.pageCount) {
                            val page = renderer.openPage(i)
                            // Render page to bitmap (using 2x scale for crisp reading)
                            val scale = 2
                            val bitmap = Bitmap.createBitmap(
                                page.width * scale,
                                page.height * scale,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()

                            val file = BitmapUtils.saveBitmapToFile(
                                context = context,
                                bitmap = bitmap,
                                format = outputFormat,
                                quality = 90,
                                prefix = "page_${i + 1}"
                            )
                            bitmap.recycle()
                            results.add(ExtractedPage(pageIndex = i, file = file))
                        }
                        renderer.close()
                        pfd.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                results
            }
            extractedPages = pages
            isExtracting = false
        }
    }

    fun savePageToDestination(file: File, destinationUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = storageManager.saveFileToDestinationUri(file, destinationUri)
            onComplete(success)
        }
    }

    fun shareSinglePage(file: File) {
        val mime = if (outputFormat == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        storageManager.shareFile(file, mime, "Share Extracted Page")
    }

    fun shareAllPages() {
        if (extractedPages.isEmpty()) return
        val files = extractedPages.map { it.file }
        val mime = if (outputFormat == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        storageManager.shareMultipleFiles(files, mime, "Share All Pages")
    }
}
