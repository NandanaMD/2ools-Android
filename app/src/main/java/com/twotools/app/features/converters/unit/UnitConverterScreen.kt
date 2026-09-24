package com.twotools.app.features.converters.unit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.twotools.app.core.designsystem.components.ToolScaffold
import java.text.DecimalFormat

enum class ConverterCategory(val title: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temp"),
    DATA("Digital Data")
}

data class ConversionUnit(val id: String, val name: String, val toBaseMultiplier: Double = 1.0)

class UnitConverterViewModel : ViewModel() {
    var selectedCategory by mutableStateOf(ConverterCategory.LENGTH)
    var inputValue by mutableStateOf("1")

    val lengthUnits = listOf(
        ConversionUnit("mm", "Millimeters (mm)", 0.001),
        ConversionUnit("cm", "Centimeters (cm)", 0.01),
        ConversionUnit("m", "Meters (m)", 1.0),
        ConversionUnit("km", "Kilometers (km)", 1000.0),
        ConversionUnit("in", "Inches (in)", 0.0254),
        ConversionUnit("ft", "Feet (ft)", 0.3048),
        ConversionUnit("yd", "Yards (yd)", 0.9144),
        ConversionUnit("mi", "Miles (mi)", 1609.344)
    )

    val weightUnits = listOf(
        ConversionUnit("mg", "Milligrams (mg)", 0.000001),
        ConversionUnit("g", "Grams (g)", 0.001),
        ConversionUnit("kg", "Kilograms (kg)", 1.0),
        ConversionUnit("t", "Metric Tons (t)", 1000.0),
        ConversionUnit("oz", "Ounces (oz)", 0.0283495),
        ConversionUnit("lb", "Pounds (lb)", 0.453592)
    )

    val dataUnits = listOf(
        ConversionUnit("B", "Bytes (B)", 1.0),
        ConversionUnit("KB", "Kilobytes (KB)", 1024.0),
        ConversionUnit("MB", "Megabytes (MB)", 1024.0 * 1024.0),
        ConversionUnit("GB", "Gigabytes (GB)", 1024.0 * 1024.0 * 1024.0),
        ConversionUnit("TB", "Terabytes (TB)", 1024.0 * 1024.0 * 1024.0 * 1024.0),
        ConversionUnit("PB", "Petabytes (PB)", 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0)
    )

    val tempUnits = listOf(
        ConversionUnit("C", "Celsius (°C)"),
        ConversionUnit("F", "Fahrenheit (°F)"),
        ConversionUnit("K", "Kelvin (K)")
    )

    var fromUnitIndex by mutableStateOf(2) // meters default
    var toUnitIndex by mutableStateOf(5)   // feet default

    fun currentUnits(): List<ConversionUnit> = when (selectedCategory) {
        ConverterCategory.LENGTH -> lengthUnits
        ConverterCategory.WEIGHT -> weightUnits
        ConverterCategory.TEMPERATURE -> tempUnits
        ConverterCategory.DATA -> dataUnits
    }

    fun swapUnits() {
        val temp = fromUnitIndex
        fromUnitIndex = toUnitIndex
        toUnitIndex = temp
    }

    fun onCategoryChange(category: ConverterCategory) {
        selectedCategory = category
        fromUnitIndex = 0
        toUnitIndex = 1
    }

    fun convert(): String {
        val value = inputValue.toDoubleOrNull() ?: return ""
        val units = currentUnits()
        val from = units.getOrNull(fromUnitIndex) ?: return ""
        val to = units.getOrNull(toUnitIndex) ?: return ""

        val result = if (selectedCategory == ConverterCategory.TEMPERATURE) {
            convertTemperature(value, from.id, to.id)
        } else {
            val baseValue = value * from.toBaseMultiplier
            baseValue / to.toBaseMultiplier
        }

        val df = DecimalFormat("#,##0.######")
        return df.format(result)
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): Double {
        val celsius = when (fromId) {
            "C" -> value
            "F" -> (value - 32.0) * (5.0 / 9.0)
            "K" -> value - 273.15
            else -> value
        }
        return when (toId) {
            "C" -> celsius
            "F" -> (celsius * (9.0 / 5.0)) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val units = viewModel.currentUnits()
    val result = viewModel.convert()

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    ToolScaffold(
        title = "Unit Converter",
        subtitle = "Length, weight, temperature, and digital data",
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
            // Category Tabs
            PrimaryTabRow(
                selectedTabIndex = viewModel.selectedCategory.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                for (category in ConverterCategory.entries) {
                    Tab(
                        selected = viewModel.selectedCategory == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        text = { Text(category.title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "From Unit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = viewModel.inputValue,
                        onValueChange = { viewModel.inputValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        label = { Text("Value to convert") }
                    )

                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = units.getOrNull(viewModel.fromUnitIndex)?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false }
                        ) {
                            units.forEachIndexed { index, unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.name) },
                                    onClick = {
                                        viewModel.fromUnitIndex = index
                                        fromExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Swap Button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FilledIconButton(
                    onClick = viewModel::swapUnits,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.SwapVert, contentDescription = "Swap units")
                }
            }

            // Output Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "To Unit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = units.getOrNull(viewModel.toUnitIndex)?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = toExpanded,
                            onDismissRequest = { toExpanded = false }
                        ) {
                            units.forEachIndexed { index, unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.name) },
                                    onClick = {
                                        viewModel.toUnitIndex = index
                                        toExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (result.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Converted Result",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$result ${units.getOrNull(viewModel.toUnitIndex)?.id ?: ""}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
