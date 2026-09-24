package com.twotools.app.features.text.hash

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.twotools.app.core.designsystem.components.ToolScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.Locale

data class HashResults(
    val md5: String = "",
    val sha1: String = "",
    val sha256: String = ""
)

class HashGeneratorViewModel : ViewModel() {
    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _hashes = MutableStateFlow(HashResults())
    val hashes: StateFlow<HashResults> = _hashes.asStateFlow()

    private val _isUppercase = MutableStateFlow(false)
    val isUppercase: StateFlow<Boolean> = _isUppercase.asStateFlow()

    fun onInputChanged(text: String) {
        _input.value = text
        computeHashes(text, _isUppercase.value)
    }

    fun toggleUppercase(uppercase: Boolean) {
        _isUppercase.value = uppercase
        computeHashes(_input.value, uppercase)
    }

    fun clear() {
        _input.value = ""
        _hashes.value = HashResults()
    }

    private fun computeHashes(text: String, upper: Boolean) {
        if (text.isEmpty()) {
            _hashes.value = HashResults()
            return
        }

        val md5 = hashWithAlgorithm(text, "MD5", upper)
        val sha1 = hashWithAlgorithm(text, "SHA-1", upper)
        val sha256 = hashWithAlgorithm(text, "SHA-256", upper)

        _hashes.value = HashResults(md5 = md5, sha1 = sha1, sha256 = sha256)
    }

    private fun hashWithAlgorithm(text: String, algorithm: String, upper: Boolean): String {
        val digest = MessageDigest.getInstance(algorithm).digest(text.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        return if (upper) hex.uppercase(Locale.getDefault()) else hex
    }
}

@Composable
fun HashGeneratorScreen(
    viewModel: HashGeneratorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val input by viewModel.input.collectAsState()
    val hashes by viewModel.hashes.collectAsState()
    val isUppercase by viewModel.isUppercase.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val copyHash: (String, String) -> Unit = { value, label ->
        clipboard.setText(AnnotatedString(value))
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    ToolScaffold(
        title = "Hash & Checksum",
        subtitle = "Generate MD5, SHA-1, and SHA-256 digests",
        onBackClick = onBackClick,
        actions = {
            if (input.isNotEmpty()) {
                IconButton(onClick = viewModel::clear) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Clear")
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
            OutlinedTextField(
                value = input,
                onValueChange = viewModel::onInputChanged,
                placeholder = { Text("Enter text to hash...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Switch for Uppercase Hex
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Uppercase Output (A-F)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isUppercase,
                    onCheckedChange = viewModel::toggleUppercase
                )
            }

            Text(
                text = "Cryptographic Hashes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HashItemCard(
                algorithm = "SHA-256 (Recommended)",
                hashValue = hashes.sha256,
                onCopy = { copyHash(hashes.sha256, "SHA-256") }
            )

            HashItemCard(
                algorithm = "SHA-1",
                hashValue = hashes.sha1,
                onCopy = { copyHash(hashes.sha1, "SHA-1") }
            )

            HashItemCard(
                algorithm = "MD5",
                hashValue = hashes.md5,
                onCopy = { copyHash(hashes.md5, "MD5") }
            )
        }
    }
}

@Composable
private fun HashItemCard(
    algorithm: String,
    hashValue: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = algorithm,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onCopy,
                    enabled = hashValue.isNotEmpty(),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy hash",
                        tint = if (hashValue.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hashValue.isNotEmpty()) hashValue else "Enter text above to compute hash",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = if (hashValue.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
