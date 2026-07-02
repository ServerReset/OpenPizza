package com.openpizza.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpizza.app.data.model.CrustType
import com.openpizza.app.data.model.PizzaSize
import com.openpizza.app.data.model.ToppingOption
import com.openpizza.app.viewmodel.MainViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuilderScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val buildState by viewModel.buildState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pizza Builder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        SectionHeader("Crust")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.crustOptions.forEach { crust ->
                FilterChip(
                    selected = buildState.crust == crust,
                    onClick = { viewModel.updateBuilderCrust(crust) },
                    label = { Text(crust.displayName) },
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        SectionHeader("Size")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.sizeOptions.forEach { size ->
                FilterChip(
                    selected = buildState.size == size,
                    onClick = { viewModel.updateBuilderSize(size) },
                    label = { Text("${size.displayName} ($${String.format("%.2f", size.basePrice)})") },
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        SectionHeader("Sauce")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.sauceOptions.forEach { sauce ->
                FilterChip(
                    selected = buildState.sauce.name == sauce.name,
                    onClick = { viewModel.updateBuilderSauce(sauce) },
                    label = { Text(sauce.name) },
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        SectionHeader("Cheese")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.cheeseOptions.forEach { cheese ->
                FilterChip(
                    selected = buildState.cheese.name == cheese.name,
                    onClick = { viewModel.updateBuilderCheese(cheese) },
                    label = { Text(cheese.name) },
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        SectionHeader("Toppings (tap to toggle)")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.toppingOptions.forEach { topping ->
                FilterChip(
                    selected = buildState.toppings.any { it.code == topping.code },
                    onClick = { viewModel.toggleTopping(topping) },
                    label = { Text(topping.name) },
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Order Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val crustName = buildState.crust?.displayName ?: "Not selected"
                val sizeName = buildState.size?.displayName ?: "Not selected"
                SummaryRow("Crust", crustName)
                SummaryRow("Size", sizeName)
                SummaryRow("Sauce", buildState.sauce.name)
                SummaryRow("Cheese", buildState.cheese.name)
                if (buildState.toppings.isNotEmpty()) {
                    SummaryRow(
                        "Toppings",
                        buildState.toppings.joinToString(", ") { it.name }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Estimated Total: $${String.format("%.2f", viewModel.getBuilderPrice())}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (buildState.crust != null && buildState.size != null) {
            Button(
                onClick = {
                    viewModel.addBuilderToCart()
                    onNavigate("cart")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    "Add to Cart",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
