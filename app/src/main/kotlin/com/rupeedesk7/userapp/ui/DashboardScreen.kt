package com.rupeedesk7.userapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.tasks.await
import com.rupeedesk7.userapp.data.UserModel
import androidx.compose.foundation.clickable

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
    var simList by remember { mutableStateOf(listOf<SubscriptionInfo>()) }
    var selectedSim by remember { mutableStateOf(-1) }

    // Permission launcher for READ_PHONE_STATE to fetch SIMs
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // reload
        } else {
            Toast.makeText(ctx, "Phone permission required for SIM list", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        // load current user phone from local saved doc if available or pick first user in collection (prototype)
        // For simplicity, use the first user doc (prototype). In production, store logged-in phone in prefs.
        val snap = db.collection("users").limit(1).get().await()
        if (!snap.isEmpty) {
            val doc = snap.documents[0]
            val u = doc.toObject<UserModel>()
            phone = u?.phone ?: doc.id
            name = u?.name ?: ""
            balance = u?.balance ?: 0.0
            dailySent = u?.dailySent ?: 0
            dailyLimit = u?.dailyLimit ?: 50
            spins = u?.spins ?: 0
            selectedSim = u?.simId ?: -1
        }

        // request permission to read SIMs
        if (ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val sm = ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            simList = sm.activeSubscriptionInfoList ?: listOf()
        } else {
            launcher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Welcome, ${'$'}name", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Balance: ₹${'$'}{String.format("%.2f", balance)}")
        Text("Sent today: ${'$'}{dailySent} / ${'$'}{dailyLimit}")
        Spacer(modifier = Modifier.height(12.dp))

        // SIM selector
        Text("Select SIM to use:")
        if (simList.isEmpty()) {
            Text("No SIM info available or permission not granted.")
        } else {
            simList.forEach { s ->
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp).clickable {
                    selectedSim = s.subscriptionId
                    // save to user doc
                    db.collection("users").document(phone).update("simId", selectedSim)
                }) {
                    Text(text = "${'$'}{s.carrierName} (${s.number ?: "hidden"})") 
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            // schedule WorkManager to send one SMS immediately
            // For prototype: call cloud function or rely on worker (not implemented fully)
            Toast.makeText(ctx, "Start sending scheduled (prototype)", Toast.LENGTH_SHORT).show()
        }) {
            Text("Start Sending (auto)")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Button(onClick = { navController.navigate("spin") }) { Text("Spin Wheel (${spins})") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("profile") }) { Text("Profile") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("withdraw") }) { Text("Withdraw") }
        }
    }
}
