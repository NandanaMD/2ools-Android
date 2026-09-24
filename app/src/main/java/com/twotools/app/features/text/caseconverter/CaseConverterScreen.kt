package com.twotools.app.features.text.caseconverter

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.twotools.app.core.designsystem.components.ToolScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class CaseConverterViewModel : ViewModel() {
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun onTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun clear() {
        _inputText.value = ""
    }

    fun toUpperCase(text: String): String = text.uppercase(Locale.getDefault())
    fun toLowerCase(text: String): String = text.lowercase(Locale.getDefault())

    fun toTitleCase(text: String): String {
        return text.split("\\s+".toRegex()).joinToString(" ") { word ->
            word.lowercase(Locale.getDefault()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    fun toCamelCase(text: String): String {
        val words = text.trim().split("[\\s_\\-]+".toRegex()).filter { it.isNotEmpty() }
        if (words.isEmpty()) return ""
        return words.first().lowercase(Locale.getDefault()) + words.drop(1).joinToString("") { word ->
            word.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase(Locale.getDefault()) }
        }
    }

    fun toSnakeCase(text: String): String {
        return text.trim()
            .split("[\\s_\\-]+".toRegex())
            .filter { it.isNotEmpty() }
            .joinToString("_") { it.lowercase(Locale.getDefault()) }
    }

    fun toKebabCase(text: String): String {
        return text.trim()
            .split("[\\s_\\-]+".toRegex())
            .filter { it.isNotEmpty() }
            .joinToString("-") { it.lowercase(Locale.getDefault()) }
    }
}

@Composable
fun CaseConverterScreen(
    viewModel: CaseConverterViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text by viewModel.inputText.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val copyToClipboard: (String, String) -> Unit = { converted, label ->
        clipboard.setText(AnnotatedString(converted))
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    ToolScaffold(
        title = "Case Converter",
        subtitle = "Format text into various casing styles",
        onBackClick = onBackClick,
        actions = {
            if (text.isNotEmpty()) {
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
                value = text,
                onValueChange = viewModel::onTextChanged,
                placeholder = { Text("Enter or paste text to convert...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            val isTextAvailable = text.isNotBlank()

            ConversionResultCard(
                title = "UPPERCASE",
                content = if (isTextAvailable) viewModel.toUpperCase(text) else "EXAMPLE TEXT",
                enabled = isTextAvailable,
                onCopy = { copyToClipboard(viewModel.toUpperCase(text), "UPPERCASE") }
            )

            ConversionResultCard(
                title = "lowercase",
                content = if (isTextAvailable) viewModel.toLowerCase(text) else "example text",
                enabled = isTextAvailable,
                onCopy = { copyToClipboard(viewModel.toLowerCase(text), "lowercase") }
            )

            ConversionResultCard(
                title = "Title Case",
                content = if (isTextAvailable) viewModel.toTitleCase(text) else "Example Text",
                enabled = isTextAvailable,
                onCopy = { copyToClipboard(viewModel.toTitleCase(text), "Title Case") }
            )

            ConversionResultCard(
                title = "camelCase",
                content = if (isTextAvailable) viewModel.toCamelCase(text) else "exampleText",
                enabled = isTextAvailable,
                onCopy = { copyToClipboard(viewModel.toCamelCase(text), "camelCase") }
            )

            ConversionResultCard(
                title = "snake_case",
                content = if (isTextAvailable) viewModel.toSnakeCase(text) else "example_text",
                enabled = isTextAvailable,
                onCopy = { copyToClipboard(viewModel.toSnakeCase(text), "snake_case") }
            )

            ConversionResultCard(
                title = "kebab-case",
                content = if (isTextAvailable) viewModel.toKebabCase(text) else "example-text",
                enabled = isTextAvailable,
                onCopy = { copyToClipboard(viewModel.toKebabCase(text), "kebab-case") }
            )
        }
    }
}

@Composable
private fun ConversionResultCard(
    title: String,
    content: String,
    enabled: Boolean,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            IconButton(
                onClick = onCopy,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy $title",
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}
