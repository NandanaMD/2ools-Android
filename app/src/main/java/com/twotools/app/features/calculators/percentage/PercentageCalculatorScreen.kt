package com.twotools.app.features.calculators.percentage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.twotools.app.core.designsystem.components.ToolScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

class PercentageViewModel : ViewModel() {
    var m1Percent by mutableStateOf("")
    var m1Value by mutableStateOf("")

    var m2Part by mutableStateOf("")
    var m2Total by mutableStateOf("")

    var m3From by mutableStateOf("")
    var m3To by mutableStateOf("")

    private val df = DecimalFormat("#,##0.##")

    fun calcMode1(): String {
        val p = m1Percent.toDoubleOrNull() ?: return ""
        val v = m1Value.toDoubleOrNull() ?: return ""
        return df.format((p / 100.0) * v)
    }

    fun calcMode2(): String {
        val part = m2Part.toDoubleOrNull() ?: return ""
        val total = m2Total.toDoubleOrNull() ?: return ""
        if (total == 0.0) return "Undefined (Div by 0)"
        return "${df.format((part / total) * 100.0)}%"
    }

    fun calcMode3(): Pair<String, Boolean> {
        val from = m3From.toDoubleOrNull() ?: return Pair("", true)
        val to = m3To.toDoubleOrNull() ?: return Pair("", true)
        if (from == 0.0) return Pair("Undefined", true)
        val diff = ((to - from) / from) * 100.0
        val isIncrease = diff >= 0
        val sign = if (isIncrease) "+" else ""
        return Pair("$sign${df.format(diff)}%", isIncrease)
    }
}

@Composable
fun PercentageCalculatorScreen(
    viewModel: PercentageViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ToolScaffold(
        title = "Percentage Calculator",
        subtitle = "Everyday discounts, taxes, and rate changes",
        onBackClick = onBackClick,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CalcSectionCard(title = "What is X% of Y?") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.m1Percent,
                        onValueChange = { viewModel.m1Percent = it },
                        label = { Text("Percentage (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.m1Value,
                        onValueChange = { viewModel.m1Value = it },
                        label = { Text("Total Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                val result = viewModel.calcMode1()
                if (result.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultBanner(label = "Result", value = result)
                }
            }

            CalcSectionCard(title = "X is what % of Y?") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.m2Part,
                        onValueChange = { viewModel.m2Part = it },
                        label = { Text("Value (X)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.m2Total,
                        onValueChange = { viewModel.m2Total = it },
                        label = { Text("Total (Y)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                val result = viewModel.calcMode2()
                if (result.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultBanner(label = "Percentage", value = result)
                }
            }

            CalcSectionCard(title = "Percentage Increase / Decrease") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.m3From,
                        onValueChange = { viewModel.m3From = it },
                        label = { Text("Initial (From)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.m3To,
                        onValueChange = { viewModel.m3To = it },
                        label = { Text("Final (To)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                val (res3, isIncrease) = viewModel.calcMode3()
                if (res3.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultBanner(
                        label = if (isIncrease) "Increase" else "Decrease",
                        value = res3
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun ResultBanner(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
