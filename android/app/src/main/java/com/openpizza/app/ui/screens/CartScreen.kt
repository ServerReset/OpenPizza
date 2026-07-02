@file:OptIn(ExperimentalMaterial3Api::class)

package com.openpizza.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openpizza.app.data.model.CustomerRequest
import com.openpizza.app.data.model.PaymentInfo
import com.openpizza.app.viewmodel.MainViewModel

@Composable
fun CartScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val cart by viewModel.cart.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val orderLoading by viewModel.orderLoading.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var storeId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardZip by remember { mutableStateOf("") }
    var tipAmount by remember { mutableStateOf("") }

    LaunchedEffect(selectedStore) {
        storeId = selectedStore?.id ?: ""
    }

    val total = remember(cart) { viewModel.getCartTotal() }
    val tax = remember(cart) { viewModel.getCartTax() }
    val grandTotal = remember(cart) { viewModel.getCartGrandTotal() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (orderResult?.placed == true) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "\uD83C\uDF89", fontSize = 24.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Order placed successfully!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item {
                Text("Your Cart", style = MaterialTheme.typography.headlineSmall)
            }

            if (cart.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("\uD83D\uDED2", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your cart is empty",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(cart) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (item.toppings.isNotBlank()) {
                                    Text(
                                        item.toppings,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (item.description.isNotBlank()) {
                                    Text(
                                        item.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = { viewModel.removeFromCart(index) },
                                modifier = Modifier.size(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("X", fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalButton(
                                    onClick = { viewModel.updateCartItemQty(index, -1) },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    enabled = item.qty > 1
                                ) {
                                    Text("-", fontSize = 16.sp)
                                }
                                Text(
                                    "${item.qty}",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                FilledTonalButton(
                                    onClick = { viewModel.updateCartItemQty(index, 1) },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 16.sp)
                                }
                            }
                            Text(
                                "\$${"%.2f".format(item.price * item.qty)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (cart.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                                Text("\$${"%.2f".format(total)}", style = MaterialTheme.typography.bodyLarge)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tax (8%)", style = MaterialTheme.typography.bodyLarge)
                                Text("\$${"%.2f".format(tax)}", style = MaterialTheme.typography.bodyLarge)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "\$${"%.2f".format(grandTotal)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Place Order", style = MaterialTheme.typography.titleLarge)
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State") },
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = zip,
                                onValueChange = { zip = it },
                                label = { Text("ZIP") },
                                modifier = Modifier.width(100.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = storeId,
                            onValueChange = { storeId = it },
                            label = { Text("Store ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = false
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Card Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("Expiry (MM/YY)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = { cardCvv = it },
                                label = { Text("CVV") },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        OutlinedTextField(
                            value = cardZip,
                            onValueChange = { cardZip = it },
                            label = { Text("Card ZIP") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = tipAmount,
                            onValueChange = { tipAmount = it },
                            label = { Text("Tip Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }

                item {
                    FilledButton(
                        onClick = {
                            val customer = CustomerRequest(
                                address = "$address, $city, $state $zip".trimStart(',', ' ').trimEnd(',', ' '),
                                firstName = profile.firstName,
                                lastName = profile.lastName,
                                phone = phone,
                                email = profile.email
                            )
                            val payment = PaymentInfo(
                                number = cardNumber,
                                expiration = cardExpiry,
                                securityCode = cardCvv,
                                postalCode = cardZip,
                                tipAmount = tipAmount.toDoubleOrNull() ?: 0.0
                            )
                            viewModel.placeOrder(customer, storeId, payment)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !orderLoading && orderResult?.placed != true
                    ) {
                        if (orderLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Place Order", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
