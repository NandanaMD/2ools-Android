package com.twotools.app.features.image.compress

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.twotools.app.core.designsystem.components.ExportActionBar
import com.twotools.app.core.designsystem.components.ImagePickerBox
import com.twotools.app.core.designsystem.components.ToolScaffold
import com.twotools.app.core.storage.StorageManager
import kotlin.math.roundToInt

@Composable
fun ImageCompressorScreen(
    viewModel: ImageCompressorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(context, uri)
        }
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        if (uri != null) {
            viewModel.saveToDestination(uri) { success ->
                val msg = if (success) "Compressed image saved!" else "Failed to save image"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ToolScaffold(
        title = "Image Compressor",
        subtitle = "Reduce image file size with controlled quality",
        onBackClick = onBackClick,
        actions = {
            if (viewModel.selectedImageUri != null) {
                IconButton(onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Change Image")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (viewModel.selectedImageUri == null) {
                ImagePickerBox(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    title = "Choose Photo to Compress",
                    subtitle = "Select any JPG, PNG, or WebP photo from your device"
                )
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = viewModel.compressedFile ?: viewModel.selectedImageUri,
                            contentDescription = "Compressed Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Original Size",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = StorageManager.formatFileSize(viewModel.originalFileSize),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                viewModel.originalDimensions?.let { (w, h) ->
                                    Text(
                                        text = "${w} × ${h} px",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Compressed Size",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (viewModel.isCompressing) "Compressing..."
                                    else StorageManager.formatFileSize(viewModel.compressedFileSize),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (viewModel.originalFileSize > 0 && viewModel.compressedFileSize > 0 && !viewModel.isCompressing) {
                                    val savings = ((1.0 - viewModel.compressedFileSize.toDouble() / viewModel.originalFileSize.toDouble()) * 100.0).roundToInt()
                                    Text(
                                        text = if (savings > 0) "-$savings% saved" else "Minimal reduction",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Compression Quality",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(
                                    text = "${viewModel.qualitySlider.toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Slider(
                            value = viewModel.qualitySlider,
                            onValueChange = { viewModel.onQualityChange(context, it) },
                            valueRange = 10f..95f,
                            steps = 16,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30 to "High (30%)", 60 to "Balanced (60%)", 80 to "Light (80%)").forEach { (q, label) ->
                                FilterChip(
                                    selected = viewModel.qualitySlider.toInt() == q,
                                    onClick = { viewModel.onQualityChange(context, q.toFloat()) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                ExportActionBar(
                    onSaveClick = {
                        createDocLauncher.launch("compressed_${System.currentTimeMillis()}.jpg")
                    },
                    onShareClick = {
                        viewModel.shareCompressedFile()
                    },
                    saveLabel = "Save JPEG",
                    shareLabel = "Share Image",
                    enabled = viewModel.compressedFile != null && !viewModel.isCompressing
                )
            }
        }
    }
}
