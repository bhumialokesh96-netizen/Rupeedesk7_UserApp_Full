package com.rupeedesk7.userapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.rupeedesk7.userapp.data.UserModel
import kotlinx.coroutines.tasks.await

@Composable
fun DashboardScreen(navController: NavController) {
    val ctx = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var dailySent by remember { mutableStateOf(0) }
    var dailyLimit by remember { mutableStateOf(50) }
    var spins by remember { mutableStateOf(0) }
    var simList by remember { mutableStateOf(emptyList<SubscriptionInfo>()) }
    var selectedSim by remember { mutableStateOf(-1) }

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(ctx, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, "Phone permission required for SIM list", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        // Load one user (prototype)
        val snap = db.collection("users").limit(1).get().await()
        if (!snap.isEmpty) {
            val doc = snap.documents[0]
            val u = doc.toObject<UserModel>()
            phone = u?.phone ?: doc.id
            name = u?.name ?: ""
            balance = u?.balance ?: 0.0
            dailySent = (u?.dailySent ?: 0L).toInt()
            dailyLimit = (u?.dailyLimit ?: 50L).toInt()
            spins = (u?.spins ?: 0L).toInt()
            selectedSim = (u?.simId ?: -1L).toInt()
        }

        // Request permission to read SIMs
        if (ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val sm =
                ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            simList = sm.activeSubscriptionInfoList ?: emptyList()
        } else {
            launcher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Welcome, $name", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Balance: ₹${String.format("%.2f", balance)}")
        Text("Sent today: $dailySent / $dailyLimit")
        Spacer(modifier = Modifier.height(12.dp))

        // SIM Selector
        Text("Select SIM to use:")
        if (simList.isEmpty()) {
            Text("No SIM info available or permission not granted.")
        } else {
            simList.forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            selectedSim = s.subscriptionId
                            db.collection("users").document(phone)
                                .update("simId", selectedSim)
                            Toast
                                .makeText(
                                    ctx,
                                    "Selected SIM: ${s.carrierName}",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                ) {
                    Text(text = "${s.carrierName} (${s.number ?: "hidden"})")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            Toast.makeText(
                ctx,
                "Auto SMS scheduling started (prototype)",
                Toast.LENGTH_SHORT
            ).show()
        }) {
            Text("Start Sending (Auto)")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate("spin") }) {
                Text("Spin Wheel ($spins)")
            }
            Button(onClick = { navController.navigate("profile") }) {
                Text("Profile")
            }
            Button(onClick = { navController.navigate("withdraw") }) {
                Text("Withdraw")
            }
        }
    }
}
