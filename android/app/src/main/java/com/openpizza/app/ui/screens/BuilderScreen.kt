package com.openpizza.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
            SectionHeader("Crust")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                viewModel.crustOptions.forEach { crust ->
                    FilterChip(
                        selected = buildState.crust == crust,
                        onClick = { viewModel.updateBuilderCrust(crust) },
                        label = { Text(crust.displayName) }
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
                        label = { Text("${size.displayName} ($${String.format("%.2f", size.basePrice)})") }
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
                        label = { Text(sauce.name) }
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
                        label = { Text(cheese.name) }
                    )
                }
            }

            SectionHeader("Toppings")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                viewModel.toppingOptions.forEach { topping ->
                    FilterChip(
                        selected = buildState.toppings.any { it.code == topping.code },
                        onClick = { viewModel.toggleTopping(topping) },
                        label = { Text(topping.name) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    val crustName = buildState.crust?.displayName ?: "Not selected"
                    val sizeName = buildState.size?.displayName ?: "Not selected"
                    Text("Crust: $crustName", style = MaterialTheme.typography.bodyMedium)
                    Text("Size: $sizeName", style = MaterialTheme.typography.bodyMedium)
                    Text("Sauce: ${buildState.sauce.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Cheese: ${buildState.cheese.name}", style = MaterialTheme.typography.bodyMedium)
                    if (buildState.toppings.isNotEmpty()) {
                        Text(
                            "Toppings: ${buildState.toppings.joinToString(", ") { it.name }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Estimated Total: $${String.format("%.2f", viewModel.getBuilderPrice())}",
                        style = MaterialTheme.typography.titleMedium,
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
                        .padding(bottom = 24.dp)
                ) {
                    Text("Add to Cart")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
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
