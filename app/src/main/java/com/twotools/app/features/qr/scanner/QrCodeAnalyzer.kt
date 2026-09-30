package com.twotools.app.features.qr.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

class QrCodeAnalyzer(
    private val onBarcodeDetected: (Result) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
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

    private var isScanning = true

    fun setScanningEnabled(enabled: Boolean) {
        isScanning = enabled
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        try {
            val yBuffer = imageProxy.planes[0].buffer
            val yBytes = ByteArray(yBuffer.remaining())
            yBuffer.get(yBytes)

            val rotation = imageProxy.imageInfo.rotationDegrees
            val (rotatedBytes, dims) = rotateYPlane(
                yBytes,
                imageProxy.width,
                imageProxy.height,
                rotation
            )

            val source = PlanarYUVLuminanceSource(
                rotatedBytes,
                dims.first,
                dims.second,
                0,
                0,
                dims.first,
                dims.second,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)

            if (result != null && isScanning) {
                onBarcodeDetected(result)
            }
        } catch (_: NotFoundException) {
            // Normal when no barcode in current frame
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.reset()
            imageProxy.close()
        }
    }

    private fun rotateYPlane(
        data: ByteArray,
        width: Int,
        height: Int,
        rotation: Int
    ): Pair<ByteArray, Pair<Int, Int>> {
        return when (rotation) {
            90 -> {
                val rotated = ByteArray(data.size)
                var i = 0
                for (x in 0 until width) {
                    for (y in height - 1 downTo 0) {
                        rotated[i++] = data[y * width + x]
                    }
                }
                rotated to (height to width)
            }
            180 -> {
                val rotated = ByteArray(data.size)
                var i = 0
                for (j in data.size - 1 downTo 0) {
                    rotated[i++] = data[j]
                }
                rotated to (width to height)
            }
            270 -> {
                val rotated = ByteArray(data.size)
                var i = 0
                for (x in width - 1 downTo 0) {
                    for (y in 0 until height) {
                        rotated[i++] = data[y * width + x]
                    }
                }
                rotated to (height to width)
            }
            else -> data to (width to height)
        }
    }
}
