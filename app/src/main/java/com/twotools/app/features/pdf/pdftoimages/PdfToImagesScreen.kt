package com.twotools.app.features.pdf.pdftoimages

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.twotools.app.core.designsystem.components.ImagePickerBox
import com.twotools.app.core.designsystem.components.ToolScaffold
import java.io.File

@Composable
fun PdfToImagesScreen(
    viewModel: PdfToImagesViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pageToSave by remember { mutableStateOf<File?>(null) }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onPdfSelected(context, uri)
        }
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            if (viewModel.outputFormat == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        )
    ) { uri ->
        val file = pageToSave
        if (uri != null && file != null) {
            viewModel.savePageToDestination(file, uri) { success ->
                val msg = if (success) "Page saved successfully!" else "Failed to save page"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ToolScaffold(
        title = "PDF to Images",
        subtitle = "Extract high-resolution pages from any PDF",
        onBackClick = onBackClick,
        actions = {
            if (viewModel.selectedPdfUri != null) {
                IconButton(onClick = { docPickerLauncher.launch(arrayOf("application/pdf")) }) {
                    Icon(Icons.Rounded.FileOpen, contentDescription = "Open Another PDF")
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
            if (viewModel.selectedPdfUri == null) {
                ImagePickerBox(
                    onClick = { docPickerLauncher.launch(arrayOf("application/pdf")) },
                    title = "Choose PDF Document",
                    subtitle = "Tap to pick any PDF file from your device storage"
                )
            } else {
                // PDF Stats Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
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
                            Column {
                                Text(
                                    text = "PDF Document",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${viewModel.pageCount} Pages Found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Format selector chips
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = viewModel.outputFormat == Bitmap.CompressFormat.JPEG,
                                    onClick = {
                                        viewModel.setFormat(Bitmap.CompressFormat.JPEG)
                                        viewModel.extractAllPages(context)
                                    },
                                    label = { Text("JPG") }
                                )
                                FilterChip(
                                    selected = viewModel.outputFormat == Bitmap.CompressFormat.PNG,
                                    onClick = {
                                        viewModel.setFormat(Bitmap.CompressFormat.PNG)
                                        viewModel.extractAllPages(context)
                                    },
                                    label = { Text("PNG") }
                                )
                            }
                        }

                        if (viewModel.isExtracting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("Rendering pages...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else if (viewModel.extractedPages.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.shareAllPages() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share All ${viewModel.extractedPages.size} Pages")
                            }
                        }
                    }
                }

                // Extracted Pages List
                viewModel.extractedPages.forEach { page ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = page.file,
                                contentDescription = "Page ${page.pageIndex + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Page ${page.pageIndex + 1}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (viewModel.outputFormat == Bitmap.CompressFormat.PNG) "PNG Image" else "JPG Image",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Save single page
                            IconButton(onClick = {
                                pageToSave = page.file
                                val ext = if (viewModel.outputFormat == Bitmap.CompressFormat.PNG) ".png" else ".jpg"
                                createDocLauncher.launch("page_${page.pageIndex + 1}$ext")
                            }) {
                                Icon(Icons.Rounded.FileDownload, contentDescription = "Save Page")
                            }

                            // Share single page
                            IconButton(onClick = { viewModel.shareSinglePage(page.file) }) {
                                Icon(Icons.Rounded.Share, contentDescription = "Share Page")
                            }
                        }
                    }
                }
            }
        }
    }
}
