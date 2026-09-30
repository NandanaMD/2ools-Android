package com.twotools.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ToolAccentColor(
    val darkContainer: Color,
    val darkOnContainer: Color,
    val lightContainer: Color,
    val lightOnContainer: Color
) {
    @Composable
    fun container(): Color = if (isSystemInDarkTheme()) darkContainer else lightContainer

    @Composable
    fun onContainer(): Color = if (isSystemInDarkTheme()) darkOnContainer else lightOnContainer
}

object ToolAccents {
    private val accents = mapOf(
        // Documents & PDF
        "images_to_pdf" to ToolAccentColor(
            darkContainer = Color(0xFF3B2323),
            darkOnContainer = Color(0xFFF2B8B5),
            lightContainer = Color(0xFFFCE8E8),
            lightOnContainer = Color(0xFF8C1D18)
        ),
        "pdf_to_images" to ToolAccentColor(
            darkContainer = Color(0xFF38291F),
            darkOnContainer = Color(0xFFF7C8A5),
            lightContainer = Color(0xFFFDF0E4),
            lightOnContainer = Color(0xFF8A3D14)
        ),

        // Image Utilities
        "image_compressor" to ToolAccentColor(
            darkContainer = Color(0xFF1E3427),
            darkOnContainer = Color(0xFFA7DAB6),
            lightContainer = Color(0xFFE4F4E7),
            lightOnContainer = Color(0xFF196134)
        ),
        "image_resizer" to ToolAccentColor(
            darkContainer = Color(0xFF1D3237),
            darkOnContainer = Color(0xFF9ED8DD),
            lightContainer = Color(0xFFE2F4F7),
            lightOnContainer = Color(0xFF16565F)
        ),
        "image_converter" to ToolAccentColor(
            darkContainer = Color(0xFF1E2E3E),
            darkOnContainer = Color(0xFFA7CAEE),
            lightContainer = Color(0xFFE3EFFB),
            lightOnContainer = Color(0xFF184D81)
        ),
        "exif_inspector" to ToolAccentColor(
            darkContainer = Color(0xFF2C243B),
            darkOnContainer = Color(0xFFD2BCF6),
            lightContainer = Color(0xFFEFE8FD),
            lightOnContainer = Color(0xFF55308F)
        ),

        // QR & Barcodes
        "qr_scanner" to ToolAccentColor(
            darkContainer = Color(0xFF1A3331),
            darkOnContainer = Color(0xFF9EE0D4),
            lightContainer = Color(0xFFE0F5F1),
            lightOnContainer = Color(0xFF145E51)
        ),
        "qr_generator" to ToolAccentColor(
            darkContainer = Color(0xFF1D3042),
            darkOnContainer = Color(0xFFA2D1F9),
            lightContainer = Color(0xFFE2F1FC),
            lightOnContainer = Color(0xFF155384)
        ),

        // Calculators
        "percentage_calc" to ToolAccentColor(
            darkContainer = Color(0xFF382E1E),
            darkOnContainer = Color(0xFFF5D69D),
            lightContainer = Color(0xFFFDF2DE),
            lightOnContainer = Color(0xFF7D5700)
        ),
        "date_age_calc" to ToolAccentColor(
            darkContainer = Color(0xFF38232D),
            darkOnContainer = Color(0xFFF4B8CD),
            lightContainer = Color(0xFFFDEAF1),
            lightOnContainer = Color(0xFF8B2850)
        ),
        "emi_calc" to ToolAccentColor(
            darkContainer = Color(0xFF233424),
            darkOnContainer = Color(0xFFA9DDB0),
            lightContainer = Color(0xFFE6F5E7),
            lightOnContainer = Color(0xFF1B6327)
        ),

        // Unit Converters
        "unit_converter" to ToolAccentColor(
            darkContainer = Color(0xFF24283D),
            darkOnContainer = Color(0xFFBCC4F5),
            lightContainer = Color(0xFFE8ECFC),
            lightOnContainer = Color(0xFF323E87)
        ),

        // Text & Security
        "text_inspector" to ToolAccentColor(
            darkContainer = Color(0xFF332435),
            darkOnContainer = Color(0xFFE4BBE8),
            lightContainer = Color(0xFFF9EAFB),
            lightOnContainer = Color(0xFF6E2D77)
        ),
        "case_converter" to ToolAccentColor(
            darkContainer = Color(0xFF352926),
            darkOnContainer = Color(0xFFE5BEB2),
            lightContainer = Color(0xFFF8ECE8),
            lightOnContainer = Color(0xFF7C3B29)
        ),
        "hash_generator" to ToolAccentColor(
            darkContainer = Color(0xFF212A3D),
            darkOnContainer = Color(0xFFAFC1F2),
            lightContainer = Color(0xFFE5ECFA),
            lightOnContainer = Color(0xFF23427E)
        ),
        "password_vault" to ToolAccentColor(
            darkContainer = Color(0xFF372F1F),
            darkOnContainer = Color(0xFFF9DB9E),
            lightContainer = Color(0xFFFCF3DF),
            lightOnContainer = Color(0xFF765A0E)
        )
    )

    private val defaultAccent = ToolAccentColor(
        darkContainer = Color(0xFF1E3A66),
        darkOnContainer = Color(0xFFD3E3FD),
        lightContainer = Color(0xFFD3E3FD),
        lightOnContainer = Color(0xFF041E49)
    )

    fun forTool(toolId: String): ToolAccentColor = accents[toolId] ?: defaultAccent
}
