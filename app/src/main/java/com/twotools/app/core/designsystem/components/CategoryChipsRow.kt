package com.twotools.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twotools.app.core.designsystem.theme.StarAmber
import com.twotools.app.core.model.ToolCategory

@Composable
fun CategoryChipsRow(
    selectedCategory: ToolCategory?,
    showFavoritesOnly: Boolean,
    favoriteCount: Int,
    onAllSelected: () -> Unit,
    onFavoritesSelected: () -> Unit,
    onCategorySelected: (ToolCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isAllSelected = !showFavoritesOnly && selectedCategory == null
        CategoryPill(
            title = "All",
            icon = Icons.Rounded.GridView,
            isSelected = isAllSelected,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAllSelected()
            }
        )

        if (favoriteCount > 0) {
            CategoryPill(
                title = "Favorites ($favoriteCount)",
                icon = Icons.Rounded.Star,
                iconTint = if (showFavoritesOnly) null else StarAmber,
                isSelected = showFavoritesOnly,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onFavoritesSelected()
                }
            )
        }

        ToolCategory.entries.forEach { category ->
            val isSelected = !showFavoritesOnly && selectedCategory == category
            CategoryPill(
                title = when (category) {
                    ToolCategory.PDF -> "PDF"
                    ToolCategory.IMAGE -> "Images"
                    ToolCategory.QR -> "QR & Barcode"
                    ToolCategory.CALCULATOR -> "Calculators"
                    ToolCategory.CONVERTER -> "Converters"
                    ToolCategory.TEXT -> "Text"
                },
                icon = category.icon,
                isSelected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCategorySelected(category)
                }
            )
        }
    }
}

@Composable
private fun CategoryPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "pillScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "pillContainer"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "pillContent"
    )

    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                fontSize = 13.sp
            )
        }
    }
}
