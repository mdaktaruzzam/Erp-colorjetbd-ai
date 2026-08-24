package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await

object FirebaseService {
    private const val TAG = "FirebaseService"
    var isFirebaseAvailable: Boolean = false
        private set

    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseAuth instance: ${e.message}")
                null
            }
        } else null

    val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseFirestore instance: ${e.message}")
                null
            }
        } else null

    /**
     * Initializes Firebase App. Wrapping in try/catch allows building and running
     * safely even if google-services.json is missing or contains placeholder values.
     */
    fun initialize(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
            isFirebaseAvailable = true
            Log.d(TAG, "Firebase successfully initialized.")
        } catch (e: Exception) {
            isFirebaseAvailable = false
            Log.w(TAG, "Firebase initialization failed: ${e.message}. Offline fallbacks will be used.")
        }
    }

    /**
     * Attempts to register/sign-in a user via Firebase Authentication.
     */
    suspend fun authenticateWithFirebase(email: String, pinCode: String): Boolean {
        val firebaseAuth = auth ?: return false
        return try {
            // Attempt standard sign in
            firebaseAuth.signInWithEmailAndPassword(email, pinCode).await()
            Log.d(TAG, "Successfully authenticated with Firebase Auth for $email")
            true
        } catch (signInException: Exception) {
            try {
                // If sign in fails, attempt registration on-the-fly for demo seamlessness
                firebaseAuth.createUserWithEmailAndPassword(email, pinCode).await()
                Log.d(TAG, "Successfully registered and authenticated new Firebase Auth user for $email")
                true
            } catch (regException: Exception) {
                Log.e(TAG, "Firebase authentication error: ${regException.message}")
                false
            }
        }
    }

    /**
     * Syncs service ticket to Firebase Firestore.
     */
    fun syncServiceTicket(ticket: ServiceTicket) {
        val db = firestore ?: return
        val ticketMap = hashMapOf(
            "id" to ticket.id,
            "ticketNumber" to ticket.ticketNumber,
            "customerId" to ticket.customerId,
            "customerName" to ticket.customerName,
            "deviceModel" to ticket.deviceModel,
            "serialNumber" to ticket.serialNumber,
            "issueDescription" to ticket.issueDescription,
            "status" to ticket.status,
            "priority" to ticket.priority,
            "createdAt" to ticket.createdAt,
            "updatedAt" to ticket.updatedAt
        )
        db.collection("service_tickets")
            .document(ticket.ticketNumber.ifEmpty { "ticket_${ticket.id}" })
            .set(ticketMap)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced service ticket ${ticket.ticketNumber} to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync service ticket to Firestore: ${e.message}")
            }
    }

    /**
     * Syncs attendance records to Firestore.
     */
    fun syncAttendance(attendance: Attendance) {
        val db = firestore ?: return
        val attendanceMap = hashMapOf(
            "id" to attendance.id,
            "userId" to attendance.userId,
            "username" to attendance.username,
            "fullName" to attendance.fullName,
            "date" to attendance.date,
            "checkInTime" to attendance.checkInTime,
            "checkOutTime" to attendance.checkOutTime,
            "checkInLat" to attendance.checkInLat,
            "checkInLng" to attendance.checkInLng,
            "checkInLocationName" to attendance.checkInLocationName,
            "checkOutLat" to attendance.checkOutLat,
            "checkOutLng" to attendance.checkOutLng,
            "checkOutLocationName" to attendance.checkOutLocationName,
            "status" to attendance.status,
            "notes" to attendance.notes
        )
        db.collection("attendance_records")
            .document("att_${attendance.id}")
            .set(attendanceMap)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced attendance ${attendance.id} to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync attendance to Firestore: ${e.message}")
            }
    }
}
