package com.twotools.app.features.qr.generator

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.twotools.app.core.designsystem.components.ExportActionBar
import com.twotools.app.core.designsystem.components.ToolScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrGeneratorScreen(
    viewModel: QrGeneratorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPassword by remember { mutableStateOf(false) }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            viewModel.saveQrCodeToDestination(context, uri) { success ->
                val msg = if (success) "QR Code saved successfully!" else "Failed to save QR Code"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ToolScaffold(
        title = "QR Code Generator",
        subtitle = "Create clean QR codes for links, text, and Wi-Fi",
        onBackClick = onBackClick,
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
            PrimaryTabRow(
                selectedTabIndex = viewModel.selectedType.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                for (type in QrType.entries) {
                    Tab(
                        selected = viewModel.selectedType == type,
                        onClick = { viewModel.onTypeChange(type) },
                        text = { Text(type.label, fontWeight = FontWeight.SemiBold) }
                    )
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
                    when (viewModel.selectedType) {
                        QrType.URL -> {
                            OutlinedTextField(
                                value = viewModel.urlInput,
                                onValueChange = { viewModel.onUrlChange(it) },
                                label = { Text("Website URL") },
                                placeholder = { Text("https://example.com") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Link, contentDescription = null)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        QrType.TEXT -> {
                            OutlinedTextField(
                                value = viewModel.textInput,
                                onValueChange = { viewModel.onTextChange(it) },
                                label = { Text("Text Content") },
                                placeholder = { Text("Enter plain text or notes...") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.TextFields, contentDescription = null)
                                },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        QrType.WIFI -> {
                            OutlinedTextField(
                                value = viewModel.wifiSsid,
                                onValueChange = { viewModel.onWifiSsidChange(it) },
                                label = { Text("Network Name (SSID)") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Wifi, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = viewModel.wifiPassword,
                                onValueChange = { viewModel.onWifiPasswordChange(it) },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("WPA/WPA2", "WEP", "None").forEach { sec ->
                                    FilterChip(
                                        selected = viewModel.wifiSecurity == sec,
                                        onClick = { viewModel.onWifiSecurityChange(sec) },
                                        label = { Text(sec) }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "QR Color",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModel.colorOptions.forEachIndexed { index, option ->
                            val isSelected = viewModel.selectedColorIndex == index
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(option.color))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.onColorChange(index) }
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val bitmap = viewModel.qrBitmap
                    if (bitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Generated QR Code",
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(16.dp)
                            )
                        }

                        ExportActionBar(
                            onSaveClick = {
                                createDocLauncher.launch("qr_code_${System.currentTimeMillis()}.png")
                            },
                            onShareClick = {
                                viewModel.shareQrCode(context)
                            },
                            saveLabel = "Save PNG",
                            shareLabel = "Share QR"
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCode2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Enter content to preview QR",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
