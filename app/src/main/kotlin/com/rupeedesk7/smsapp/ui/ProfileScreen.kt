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

@Composable
fun ProfileScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val snap = db.collection("users").limit(1).get().await()
        if (!snap.isEmpty) {
            val doc = snap.documents[0]
            phone = doc.getString("phone") ?: doc.id
            name = doc.getString("name") ?: ""
            val bank = doc.get("bank") as? Map<*, *>
            bankName = bank?.get("name") as? String ?: ""
            account = bank?.get("account") as? String ?: ""
            ifsc = bank?.get("ifsc") as? String ?: ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.Start) {
        Text("Profile", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank name") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = account, onValueChange = { account = it }, label = { Text("Account number") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = ifsc, onValueChange = { ifsc = it }, label = { Text("IFSC") })
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            val data = mapOf("name" to name, "bank" to mapOf("name" to bankName, "account" to account, "ifsc" to ifsc))
            db.collection("users").document(phone).update(data as Map<String, Any>)
        }) {
            Text("Save")
        }
    }
}
