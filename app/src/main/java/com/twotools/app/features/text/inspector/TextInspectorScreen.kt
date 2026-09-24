package com.twotools.app.features.text.inspector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.twotools.app.core.designsystem.components.ToolScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TextStats(
    val characterCount: Int = 0,
    val characterCountNoSpaces: Int = 0,
    val wordCount: Int = 0,
    val sentenceCount: Int = 0,
    val lineCount: Int = 0,
    val readingTimeSeconds: Int = 0,
    val speakingTimeSeconds: Int = 0
)

class TextInspectorViewModel : ViewModel() {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _stats = MutableStateFlow(TextStats())
    val stats: StateFlow<TextStats> = _stats.asStateFlow()

    fun onTextChanged(newText: String) {
        _text.value = newText
        computeStats(newText)
    }

    fun clearText() {
        onTextChanged("")
    }

    private fun computeStats(raw: String) {
        if (raw.isEmpty()) {
            _stats.value = TextStats()
            return
        }

        val chars = raw.length
        val charsNoSpaces = raw.count { !it.isWhitespace() }
        val words = raw.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
        val sentences = raw.split("[.!?]+".toRegex()).count { it.trim().isNotEmpty() }
        val lines = raw.lines().count()

        // Average reading speed: 200 words per minute
        val readingSeconds = ((words / 200.0) * 60).toInt().coerceAtLeast(if (words > 0) 1 else 0)
        // Average speaking speed: 130 words per minute
        val speakingSeconds = ((words / 130.0) * 60).toInt().coerceAtLeast(if (words > 0) 1 else 0)

        _stats.value = TextStats(
            characterCount = chars,
            characterCountNoSpaces = charsNoSpaces,
            wordCount = words,
            sentenceCount = sentences,
            lineCount = lines,
            readingTimeSeconds = readingSeconds,
            speakingTimeSeconds = speakingSeconds
        )
    }
}

@Composable
fun TextInspectorScreen(
    viewModel: TextInspectorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text by viewModel.text.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    ToolScaffold(
        title = "Text Inspector",
        subtitle = "Detailed text analytics & word count",
        onBackClick = onBackClick,
        actions = {
            if (text.isNotEmpty()) {
                IconButton(onClick = viewModel::clearText) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Clear text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            // Text Input Box
            OutlinedTextField(
                value = text,
                onValueChange = viewModel::onTextChanged,
                placeholder = { Text("Type or paste text here to inspect statistics...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Paste button row if empty
            if (text.isEmpty()) {
                FilledTonalButton(
                    onClick = {
                        clipboardManager.getText()?.text?.let { viewModel.onTextChanged(it) }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paste from Clipboard")
                }
            }

            // Stats Cards Grid
            Text(
                text = "Metrics & Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(title = "Words", value = stats.wordCount.toString(), modifier = Modifier.weight(1f))
                MetricCard(title = "Characters", value = stats.characterCount.toString(), modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(title = "No Spaces", value = stats.characterCountNoSpaces.toString(), modifier = Modifier.weight(1f))
                MetricCard(title = "Sentences", value = stats.sentenceCount.toString(), modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(title = "Lines", value = stats.lineCount.toString(), modifier = Modifier.weight(1f))
                MetricCard(
                    title = "Reading Time",
                    value = formatDuration(stats.readingTimeSeconds),
                    modifier = Modifier.weight(1f)
                )
            }

            MetricCard(
                title = "Speaking Time (estimated)",
                value = formatDuration(stats.speakingTimeSeconds),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    if (totalSeconds < 60) return "${totalSeconds}s"
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return if (secs == 0) "${mins}m" else "${mins}m ${secs}s"
}
