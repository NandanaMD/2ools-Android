package com.twotools.app.features.qr.scanner

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

data class ScanResultData(
    val rawValue: String,
    val formatName: String,
    val valueType: Int,
    val displayValue: String = rawValue
)

class QrScannerViewModel : ViewModel() {

    var scanResult by mutableStateOf<ScanResultData?>(null)
        private set

    var isTorchEnabled by mutableStateOf(false)
        private set

    var isProcessingGallery by mutableStateOf(false)
        private set

    fun onBarcodeDetected(barcode: Barcode) {
        val raw = barcode.rawValue ?: barcode.displayValue ?: return
        val formatName = getFormatName(barcode.format)
        scanResult = ScanResultData(
            rawValue = raw,
            formatName = formatName,
            valueType = barcode.valueType,
            displayValue = barcode.displayValue ?: raw
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
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        isProcessingGallery = false
                        if (barcodes.isNotEmpty()) {
                            onBarcodeDetected(barcodes.first())
                        } else {
                            onNoBarcode()
                        }
                    }
                    .addOnFailureListener {
                        isProcessingGallery = false
                        onNoBarcode()
                    }
            } catch (e: Exception) {
                isProcessingGallery = false
                onNoBarcode()
            }
        }
    }

    private fun getFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_EAN_13 -> "EAN-13 Barcode"
            Barcode.FORMAT_EAN_8 -> "EAN-8 Barcode"
            Barcode.FORMAT_UPC_A -> "UPC-A Barcode"
            Barcode.FORMAT_UPC_E -> "UPC-E Barcode"
            Barcode.FORMAT_CODE_128 -> "Code 128 Barcode"
            Barcode.FORMAT_CODE_39 -> "Code 39 Barcode"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_AZTEC -> "Aztec Code"
            Barcode.FORMAT_PDF417 -> "PDF417 Code"
            else -> "Barcode / QR"
        }
    }
}
