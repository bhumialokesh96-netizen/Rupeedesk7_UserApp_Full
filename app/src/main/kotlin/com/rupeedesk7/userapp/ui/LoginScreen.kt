package com.rupeedesk7.userapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    val ctx = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (+91...)" ) })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name (optional)") })
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            if (phone.isBlank()) {
                Toast.makeText(ctx, "Enter phone", Toast.LENGTH_SHORT).show()
                return@Button
            }
            val db = FirebaseFirestore.getInstance()
            val doc = hashMapOf(
                "phone" to phone,
                "name" to name,
                "balance" to 0.0,
                "dailySent" to 0,
                "dailyLimit" to 50,
                "spins" to 0,
                "referredBy" to null,
                "simId" to -1
            )
            db.collection("users").document(phone).set(doc).addOnSuccessListener {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(ctx, "Error: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            }
        }) {
            Text("Login / Register")
        }
    }
}
