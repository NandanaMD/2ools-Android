package com.twotools.app.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    PDF("Documents & PDF", Icons.Rounded.PictureAsPdf, "Create, convert, and manage PDF documents"),
    IMAGE("Image Utilities", Icons.Rounded.Image, "Compress, resize, and convert image formats"),
    QR("QR & Barcodes", Icons.Rounded.QrCode, "Fast scanning and instant code generation"),
    CALCULATOR("Calculators", Icons.Rounded.Calculate, "Financial, date, and everyday math calculations"),
    CONVERTER("Unit Converters", Icons.Rounded.SwapHoriz, "Convert length, mass, temperature, and data"),
    TEXT("Text & Security", Icons.Rounded.TextFields, "Inspect text, convert case, and generate checksums")
}

data class Tool(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val route: String,
    val keywords: List<String> = emptyList(),
    val badge: String? = null
)

sealed interface ProcessState<out T> {
    data object Idle : ProcessState<Nothing>
    data class Processing(val progress: Float? = null, val message: String? = null) : ProcessState<Nothing>
    data class Success<out T>(val data: T) : ProcessState<T>
    data class Error(val message: String) : ProcessState<Nothing>
}
