package com.twotools.app.features.calculators.emi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.twotools.app.core.designsystem.theme.Cyan400
import com.twotools.app.core.designsystem.theme.Indigo500
import java.text.DecimalFormat
import kotlin.math.pow

data class EmiResult(
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val principalRatio: Float
)

class EmiViewModel : ViewModel() {
    var principal by mutableStateOf("50000")
    var interestRate by mutableStateOf("8.5")
    var tenureYears by mutableStateOf("5")

    private val df = DecimalFormat("#,##0.00")

    fun calculateEmi(): EmiResult? {
        val p = principal.toDoubleOrNull() ?: return null
        val annualRate = interestRate.toDoubleOrNull() ?: return null
        val years = tenureYears.toDoubleOrNull() ?: return null

        if (p <= 0 || annualRate < 0 || years <= 0) return null

        val r = (annualRate / 12.0) / 100.0
        val n = years * 12.0

        val emi = if (r == 0.0) {
            p / n
        } else {
            (p * r * (1.0 + r).pow(n)) / ((1.0 + r).pow(n) - 1.0)
        }

        val totalPayment = emi * n
        val totalInterest = totalPayment - p
        val principalRatio = (p / totalPayment).toFloat().coerceIn(0f, 1f)

        return EmiResult(
            monthlyEmi = emi,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            principalRatio = principalRatio
        )
    }

    fun formatCurrency(amount: Double): String = df.format(amount)
}

@Composable
fun EmiCalculatorScreen(
    viewModel: EmiViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = viewModel.calculateEmi()

    ToolScaffold(
        title = "Loan / EMI Calculator",
        subtitle = "Calculate loan installments and interest breakdown",
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
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Loan Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = viewModel.principal,
                        onValueChange = { viewModel.principal = it },
                        label = { Text("Loan Amount (Principal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.interestRate,
                            onValueChange = { viewModel.interestRate = it },
                            label = { Text("Interest Rate (% p.a.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = viewModel.tenureYears,
                            onValueChange = { viewModel.tenureYears = it },
                            label = { Text("Tenure (Years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (result != null) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Monthly EMI Payment",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = viewModel.formatCurrency(result.monthlyEmi),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        Text(
                            text = "Payment Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(result.principalRatio)
                                    .fillMaxHeight()
                                    .background(Indigo500)
                            )
                            Box(
                                modifier = Modifier
                                    .weight((1f - result.principalRatio).coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(Cyan400)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Principal Amount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatCurrency(viewModel.principal.toDoubleOrNull() ?: 0.0),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Interest",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatCurrency(result.totalInterest),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Cyan400
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.formatCurrency(result.totalPayment),
                                style = MaterialTheme.typography.titleMedium,
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
