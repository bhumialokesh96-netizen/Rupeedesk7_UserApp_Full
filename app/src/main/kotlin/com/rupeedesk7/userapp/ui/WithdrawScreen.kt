package com.rupeedesk7.smsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun WithdrawScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var phone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Load user phone when screen starts
    LaunchedEffect(Unit) {
        try {
            val snap = db.collection("users").limit(1).get().await()
            if (!snap.isEmpty) {
                val doc = snap.documents[0]
                phone = doc.getString("phone") ?: doc.id
            }
        } catch (e: Exception) {
            message = "Error loading user: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Withdraw", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount <= 0.0) {
                    message = "Please enter a valid amount"
                    return@Button
                }

                isSubmitting = true
                val req = mapOf(
                    "userPhone" to phone,
                    "amount" to amount,
                    "status" to "pending",
                    "createdAt" to Timestamp.now()
                )

                db.collection("withdrawals").add(req)
                    .addOnSuccessListener {
                        message = "Withdrawal request submitted"
                        isSubmitting = false
                        amountText = ""
                    }
                    .addOnFailureListener { e ->
                        message = "Error: ${e.message}"
                        isSubmitting = false
                    }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Request Withdrawal")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Text(message, style = MaterialTheme.typography.body2)
        }
    }
}
