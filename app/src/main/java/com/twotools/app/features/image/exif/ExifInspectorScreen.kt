package com.twotools.app.features.image.exif

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.twotools.app.core.designsystem.components.ExportActionBar
import com.twotools.app.core.designsystem.components.ImagePickerBox
import com.twotools.app.core.designsystem.components.ToolScaffold

@Composable
fun ExifInspectorScreen(
    viewModel: ExifInspectorViewModel,
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
            viewModel.saveCleanFileToDestination(uri) { success ->
                val msg = if (success) "Clean image saved!" else "Failed to save image"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ToolScaffold(
        title = "EXIF Metadata Stripper",
        subtitle = "Inspect and strip privacy and GPS metadata",
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
                    title = "Choose Photo to Inspect",
                    subtitle = "Select any photo to inspect its embedded camera and GPS tags"
                )
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = viewModel.selectedImageUri,
                            contentDescription = "Photo thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Metadata Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val meta = viewModel.metadata
                            val locationStatus = if (meta?.hasLocation == true) "Location Data Found" else "No Location Tagged"
                            Text(
                                text = locationStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (meta?.hasLocation == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val meta = viewModel.metadata
                if (meta != null) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = if (meta.hasLocation) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "GPS Location",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (meta.hasLocation) {
                                Text(
                                    text = "Latitude: ${meta.latitude}, Longitude: ${meta.longitude}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                meta.altitude?.let {
                                    Text(
                                        text = "Altitude: ${"%.1f".format(it)} meters",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val geoUri = Uri.parse("geo:${meta.latitude},${meta.longitude}?q=${meta.latitude},${meta.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(mapIntent)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Maps")
                                }
                            } else {
                                Text(
                                    text = "No GPS coordinates embedded in this photo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Camera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Camera & Shot Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            MetadataRow(label = "Camera Model", value = listOfNotNull(meta.cameraMake, meta.cameraModel).joinToString(" "))
                            MetadataRow(label = "Date Taken", value = meta.dateTime)
                            MetadataRow(label = "Aperture", value = meta.fNumber)
                            MetadataRow(label = "Exposure Time", value = meta.exposureTime)
                            MetadataRow(label = "ISO Speed", value = meta.iso)
                            MetadataRow(label = "Focal Length", value = meta.focalLength)
                            if (meta.width != null && meta.height != null) {
                                MetadataRow(label = "Resolution", value = "${meta.width} × ${meta.height} px")
                            }
                        }
                    }

                    if (viewModel.strippedFile == null) {
                        Button(
                            onClick = {
                                viewModel.stripMetadata(context) {
                                    Toast.makeText(context, "Sanitized photo ready!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Strip All Metadata (Privacy Clean)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Photo Sanitized!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "All GPS coordinates, device models, and timestamp tags have been stripped. Safe to post or share online.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        ExportActionBar(
                            onSaveClick = {
                                createDocLauncher.launch("clean_${System.currentTimeMillis()}.jpg")
                            },
                            onShareClick = {
                                viewModel.shareCleanFile()
                            },
                            saveLabel = "Save Clean Photo",
                            shareLabel = "Share",
                            enabled = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
