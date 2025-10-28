package com.rupeedesk7.smsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

@Composable
fun SpinScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var phone by remember { mutableStateOf("") }
    var spins by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val snap = db.collection("users").limit(1).get().await()
        if (!snap.isEmpty) {
            val doc = snap.documents[0]
            phone = doc.getString("phone") ?: doc.id
            spins = (doc.getLong("spins") ?: 0).toInt()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Spin Wheel", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Spins available: ${'$'}spins")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            if (spins <= 0) {
                message = "No spins left"
                return@Button
            }
            // simple client-side random reward (prototype) - production should use server-side verification
            val prize = Random.nextInt(0, 3)
            when(prize) {
                0 -> { // coins
                    val amt = 1.0
                    db.collection("users").document(phone).update("balance", com.google.firebase.firestore.FieldValue.increment(amt))
                    message = "You won ₹${'$'}amt"
                }
                1 -> { // extra spin
                    db.collection("users").document(phone).update("spins", com.google.firebase.firestore.FieldValue.increment(1))
                    message = "You won 1 extra spin"
                }
                2 -> {
                    db.collection("users").document(phone).update("dailyLimit", com.google.firebase.firestore.FieldValue.increment(5))
                    message = "Daily limit +5"
                }
            }
            // decrement spin
            db.collection("users").document(phone).update("spins", com.google.firebase.firestore.FieldValue.increment(-1))
        }) {
            Text("Spin Now")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(message)
    }
}
