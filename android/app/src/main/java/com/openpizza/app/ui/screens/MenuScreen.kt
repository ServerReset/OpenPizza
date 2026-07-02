package com.openpizza.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.openpizza.app.data.model.CartItem
import com.openpizza.app.data.model.Category
import com.openpizza.app.data.model.MenuItem
import com.openpizza.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categoryEmoji: (String) -> String = { id ->
        when (id) {
            "pizza" -> "\uD83C\uDF55"
            "sandwiches" -> "\uD83E\uDD6A"
            "pasta" -> "\uD83C\uDF5D"
            "sides" -> "\uD83E\uDD5F"
            "desserts" -> "\uD83C\uDF6A"
            "drinks" -> "\uD83E\uDD64"
            "wings" -> "\uD83C\uDF57"
            "salads" -> "\uD83E\uDD57"
            else -> "\uD83C\uDF7D\uFE0F"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedCategory != null) {
            TextButton(onClick = { selectedCategory = null }) {
                Text("\u2190 All Categories")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category.id,
                    onClick = { selectedCategory = category.id },
                    label = {
                        Text("${categoryEmoji(category.id)} ${category.name}")
                    }
                )
            }
        }

        val filteredItems = selectedCategory?.let { viewModel.getMenuItemsByCategory(it) } ?: emptyList()

        if (selectedCategory != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    MenuItemCard(
                        item = item,
                        emoji = categoryEmoji(selectedCategory ?: ""),
                        onClick = {
                            val price = item.Price?.toDoubleOrNull() ?: 0.0
                            viewModel.addItemToCart(
                                CartItem(
                                    code = item.ProductCode ?: "",
                                    name = item.Name ?: "Unknown",
                                    price = price,
                                    description = item.Description ?: ""
                                )
                            )
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = category.id }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = categoryEmoji(category.id),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = category.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem, emoji: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.Name ?: "Unknown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.Description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$${String.format("%.2f", item.Price?.toDoubleOrNull() ?: 0.0)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
