package com.rupeedesk7.userapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WithdrawScreen(navController: androidx.navigation.NavController) {
    val db = FirebaseFirestore.getInstance()
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(0.0) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val snap = db.collection("users").limit(1).get().await()
        if (!snap.isEmpty) {
            val doc = snap.documents[0]
            phone = doc.getString("phone") ?: doc.id
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.Start) {
        Text("Withdraw", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = if (amount==0.0) "" else amount.toString(), onValueChange = { amount = it.toDoubleOrNull() ?: 0.0 }, label = { Text("Amount") })
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (amount <= 0.0) {
                message = "Enter valid amount"
                return@Button
            }
            val req = mapOf("userPhone" to phone, "amount" to amount, "status" to "pending", "createdAt" to com.google.firebase.Timestamp.now())
            db.collection("withdrawals").add(req).addOnSuccessListener {
                message = "Request submitted"
            }.addOnFailureListener { e -> message = "Error: ${'$'}{e.message}" }
        }) {
            Text("Request Withdrawal")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(message)
    }
}
