package com.twotools.app.core.registry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.twotools.app.core.model.Tool
import com.twotools.app.core.model.ToolCategory

object ToolRegistry {

    val tools: List<Tool> = listOf(
        // Documents & PDF
        Tool(
            id = "images_to_pdf",
            title = "Images to PDF",
            description = "Combine multiple gallery photos into a single clean PDF",
            category = ToolCategory.PDF,
            icon = Icons.Rounded.PictureAsPdf,
            route = "tool/images_to_pdf",
            keywords = listOf("pdf", "image", "photo", "convert", "combine", "merge", "doc"),
            badge = "Popular"
        ),
        Tool(
            id = "pdf_to_images",
            title = "PDF to Images",
            description = "Extract pages of any PDF document as JPG or PNG images",
            category = ToolCategory.PDF,
            icon = Icons.Rounded.Collections,
            route = "tool/pdf_to_images",
            keywords = listOf("pdf", "extract", "pages", "images", "jpg", "png")
        ),

        // Image Utilities
        Tool(
            id = "image_compressor",
            title = "Image Compressor",
            description = "Reduce photo file sizes by up to 80% without noticeable quality loss",
            category = ToolCategory.IMAGE,
            icon = Icons.Rounded.Compress,
            route = "tool/image_compressor",
            keywords = listOf("image", "compress", "shrink", "size", "photo", "reduce", "kb", "mb"),
            badge = "Essential"
        ),
        Tool(
            id = "image_resizer",
            title = "Image Resizer",
            description = "Scale photo dimensions and pixels with aspect-ratio locking",
            category = ToolCategory.IMAGE,
            icon = Icons.Rounded.Crop,
            route = "tool/image_resizer",
            keywords = listOf("image", "resize", "scale", "dimensions", "width", "height")
        ),
        Tool(
            id = "image_converter",
            title = "Format Converter",
            description = "Convert images seamlessly between JPG, PNG, and WebP",
            category = ToolCategory.IMAGE,
            icon = Icons.Rounded.Transform,
            route = "tool/image_converter",
            keywords = listOf("image", "format", "convert", "jpg", "png", "webp")
        ),
        Tool(
            id = "exif_inspector",
            title = "EXIF Metadata Stripper",
            description = "Inspect camera and GPS data, and export clean privacy-safe photos",
            category = ToolCategory.IMAGE,
            icon = Icons.Rounded.Shield,
            route = "tool/exif_inspector",
            keywords = listOf("exif", "metadata", "privacy", "strip", "clean", "gps", "camera"),
            badge = "Privacy"
        ),

        // QR & Barcodes
        Tool(
            id = "qr_scanner",
            title = "QR & Barcode Scanner",
            description = "Rapidly scan QR codes, Wi-Fi connections, URLs, and retail barcodes",
            category = ToolCategory.QR,
            icon = Icons.Rounded.QrCodeScanner,
            route = "tool/qr_scanner",
            keywords = listOf("qr", "barcode", "scan", "camera", "wifi", "url", "code"),
            badge = "Camera"
        ),
        Tool(
            id = "qr_generator",
            title = "QR Code Generator",
            description = "Generate high-resolution QR codes for Wi-Fi networks, URLs, and text",
            category = ToolCategory.QR,
            icon = Icons.Rounded.QrCode,
            route = "tool/qr_generator",
            keywords = listOf("qr", "generate", "create", "wifi", "link", "url", "share")
        ),

        // Calculators
        Tool(
            id = "percentage_calc",
            title = "Percentage Calculator",
            description = "Calculate percentage of values, percentage changes, and discounts",
            category = ToolCategory.CALCULATOR,
            icon = Icons.Rounded.Percent,
            route = "tool/percentage_calc",
            keywords = listOf("percent", "percentage", "discount", "tax", "tip", "rate")
        ),
        Tool(
            id = "date_age_calc",
            title = "Date & Age Calculator",
            description = "Calculate exact age in years, months, and days, or days between dates",
            category = ToolCategory.CALCULATOR,
            icon = Icons.Rounded.CalendarMonth,
            route = "tool/date_age_calc",
            keywords = listOf("age", "date", "calendar", "birthday", "difference", "duration", "days")
        ),
        Tool(
            id = "emi_calc",
            title = "Loan / EMI Calculator",
            description = "Compute monthly installments, total interest, and payout schedule",
            category = ToolCategory.CALCULATOR,
            icon = Icons.Rounded.AccountBalance,
            route = "tool/emi_calc",
            keywords = listOf("emi", "loan", "mortgage", "interest", "finance", "money", "bank")
        ),

        // Unit Converters
        Tool(
            id = "unit_converter",
            title = "Universal Unit Converter",
            description = "Convert Length, Mass, Temperature, and Digital Storage units",
            category = ToolCategory.CONVERTER,
            icon = Icons.Rounded.SwapHoriz,
            route = "tool/unit_converter",
            keywords = listOf("convert", "unit", "length", "weight", "mass", "temperature", "data", "storage", "bytes")
        ),

        // Text & Security
        Tool(
            id = "text_inspector",
            title = "Text Inspector",
            description = "Real-time count of words, characters, sentences, and reading time",
            category = ToolCategory.TEXT,
            icon = Icons.Rounded.Analytics,
            route = "tool/text_inspector",
            keywords = listOf("text", "words", "characters", "counter", "reading", "sentences")
        ),
        Tool(
            id = "case_converter",
            title = "Case Converter",
            description = "Transform text to UPPERCASE, lowercase, Title Case, camelCase, snake_case",
            category = ToolCategory.TEXT,
            icon = Icons.Rounded.TextFields,
            route = "tool/case_converter",
            keywords = listOf("case", "text", "upper", "lower", "camel", "snake", "title")
        ),
        Tool(
            id = "hash_generator",
            title = "Hash & Checksum Generator",
            description = "Generate cryptographic MD5, SHA-1, and SHA-256 digests offline",
            category = ToolCategory.TEXT,
            icon = Icons.Rounded.Key,
            route = "tool/hash_generator",
            keywords = listOf("hash", "sha256", "md5", "sha1", "checksum", "crypto", "digest")
        ),
        Tool(
            id = "password_vault",
            title = "Password Vault & Generator",
            description = "100% offline encrypted credentials manager and strong password generator",
            category = ToolCategory.TEXT,
            icon = Icons.Rounded.Password,
            route = "tool/password_vault",
            keywords = listOf("password", "vault", "manager", "generator", "credentials", "login", "secret", "security"),
            badge = "Offline"
        )
    )

    fun getToolById(id: String): Tool? = tools.find { it.id == id }

    fun getToolsByCategory(category: ToolCategory): List<Tool> =
        tools.filter { it.category == category }

    fun searchTools(query: String): List<Tool> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return tools
        return tools.filter { tool ->
            tool.title.lowercase().contains(trimmed) ||
            tool.description.lowercase().contains(trimmed) ||
            tool.category.title.lowercase().contains(trimmed) ||
            tool.keywords.any { it.contains(trimmed) }
        }
    }
}
