package com.rupeedesk7.smsapp.worker

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SmsWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    private val db = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        try {
            // Query one unsent inventory
            val snap = db.collection("inventory").whereEqualTo("sent", false).limit(1).get().await()
            if (snap.isEmpty) return Result.success()
            val doc = snap.documents[0]
            val invRef = doc.reference

            // Run transaction to claim and mark sent
            val txResult = db.runTransaction { t ->
                val snapshot = t.get(invRef)
                val already = snapshot.getBoolean("sent") ?: false
                if (already) throw Exception("already sent")
                t.update(invRef, mapOf("sent" to true, "claimedAt" to FieldValue.serverTimestamp()))
                true
            }.await()

            // send SMS (device must have SIM and permission)
            val target = doc.getString("target") ?: return Result.success()
            val message = doc.getString("message") ?: ""
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(target, null, message, null, null)
            } catch (e: Exception) {
                Log.e("SmsWorker", "SMS send failed: ${'$'}{e.message}")
            }

            // Optionally update stats (prototype doesn't know which user performed send)
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
