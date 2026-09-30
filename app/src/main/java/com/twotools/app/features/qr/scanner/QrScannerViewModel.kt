package com.twotools.app.features.qr.scanner

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanResultData(
    val rawValue: String,
    val formatName: String,
    val isUrl: Boolean,
    val displayValue: String = rawValue
)

class QrScannerViewModel : ViewModel() {

    var scanResult by mutableStateOf<ScanResultData?>(null)
        private set

    var isTorchEnabled by mutableStateOf(false)
        private set

    var isProcessingGallery by mutableStateOf(false)
        private set

    fun onBarcodeDetected(result: Result) {
        val raw = result.text ?: return
        val formatName = getFormatName(result.barcodeFormat)
        val isUrl = raw.startsWith("http://", ignoreCase = true) ||
                raw.startsWith("https://", ignoreCase = true) ||
                raw.startsWith("www.", ignoreCase = true)

        scanResult = ScanResultData(
            rawValue = raw,
            formatName = formatName,
            isUrl = isUrl,
            displayValue = raw
        )
    }

    fun toggleTorch() {
        isTorchEnabled = !isTorchEnabled
    }

    fun resumeScanning() {
        scanResult = null
    }

    fun scanFromGallery(context: Context, uri: Uri, onNoBarcode: () -> Unit) {
        viewModelScope.launch {
            isProcessingGallery = true
            val decoded = withContext(Dispatchers.IO) {
                decodeBitmapFromUri(context, uri)
            }
            isProcessingGallery = false

            if (decoded != null) {
                onBarcodeDetected(decoded)
            } else {
                onNoBarcode()
            }
        }
    }

    private fun decodeBitmapFromUri(context: Context, uri: Uri): Result? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } ?: return null

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val reader = MultiFormatReader().apply {
                setHints(
                    mapOf(
                        DecodeHintType.POSSIBLE_FORMATS to listOf(
                            BarcodeFormat.QR_CODE,
                            BarcodeFormat.DATA_MATRIX,
                            BarcodeFormat.AZTEC,
                            BarcodeFormat.PDF_417,
                            BarcodeFormat.EAN_13,
                            BarcodeFormat.EAN_8,
                            BarcodeFormat.UPC_A,
                            BarcodeFormat.UPC_E,
                            BarcodeFormat.CODE_128,
                            BarcodeFormat.CODE_39,
                            BarcodeFormat.ITF
                        ),
                        DecodeHintType.TRY_HARDER to true
                    )
                )
            }

            try {
                reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
            } catch (_: NotFoundException) {
                try {
                    reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
                } catch (_: NotFoundException) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFormatName(format: BarcodeFormat): String {
        return when (format) {
            BarcodeFormat.QR_CODE -> "QR Code"
            BarcodeFormat.EAN_13 -> "EAN-13 Barcode"
            BarcodeFormat.EAN_8 -> "EAN-8 Barcode"
            BarcodeFormat.UPC_A -> "UPC-A Barcode"
            BarcodeFormat.UPC_E -> "UPC-E Barcode"
            BarcodeFormat.CODE_128 -> "Code 128 Barcode"
            BarcodeFormat.CODE_39 -> "Code 39 Barcode"
            BarcodeFormat.DATA_MATRIX -> "Data Matrix"
            BarcodeFormat.AZTEC -> "Aztec Code"
            BarcodeFormat.PDF_417 -> "PDF417 Code"
            BarcodeFormat.ITF -> "ITF Barcode"
            else -> "Barcode / QR"
        }
    }
}
