package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object FirebaseService {
    private const val TAG = "FirebaseService"
    var isFirebaseAvailable: Boolean = false
        private set

    private val _isOfflineSyncActive = MutableStateFlow(true)
    val isOfflineSyncActive: StateFlow<Boolean> = _isOfflineSyncActive.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

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
     * Initializes Firebase App with offline disk persistence enabled.
     * Even if factory network drops or is absent, writes are committed to local SQLite/disk
     * and automatically queued & uploaded when a network connection resumes.
     */
    fun initialize(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
            
            // Configure robust persistent disk cache for offline factory environment
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()

            val firestoreInstance = FirebaseFirestore.getInstance()
            firestoreInstance.firestoreSettings = settings
            isFirebaseAvailable = true
            _isOfflineSyncActive.value = true
            Log.d(TAG, "Firebase initialized with offline disk persistence.")
        } catch (e: Exception) {
            isFirebaseAvailable = false
            Log.w(TAG, "Firebase initialization failed: ${e.message}. Local Room database offline fallback will be active.")
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
     * Syncs a production record directly to Firestore with offline caching.
     * Even if the device has zero connectivity in the factory, Firestore buffers
     * the write to disk and applies local latency compensation immediately.
     */
    fun syncProductionRecord(record: ProductionRecord) {
        val db = firestore ?: return
        val docId = if (record.id > 0) "prod_${record.id}" else "prod_${record.date}_${record.machineId}_${System.currentTimeMillis()}"
        val recordMap = hashMapOf(
            "id" to record.id,
            "date" to record.date,
            "machineId" to record.machineId,
            "machineName" to record.machineName,
            "output" to record.output,
            "target" to record.target,
            "operatorName" to record.operatorName,
            "notes" to record.notes,
            "syncedAt" to System.currentTimeMillis()
        )

        db.collection("production_records")
            .document(docId)
            .set(recordMap, SetOptions.merge())
            .addOnSuccessListener {
                _lastSyncTimestamp.value = System.currentTimeMillis()
                Log.d(TAG, "Production record synced to Firestore (offline disk / cloud).")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Queued to Firestore local cache: ${e.message}")
            }
    }

    /**
     * Syncs all existing production records to Firestore batch/set for initial synchronization.
     */
    fun syncAllProductionRecords(records: List<ProductionRecord>) {
        val db = firestore ?: return
        val batch = db.batch()
        records.forEach { record ->
            val docId = if (record.id > 0) "prod_${record.id}" else "prod_${record.date}_${record.machineId}_${System.currentTimeMillis()}"
            val docRef = db.collection("production_records").document(docId)
            val recordMap = hashMapOf(
                "id" to record.id,
                "date" to record.date,
                "machineId" to record.machineId,
                "machineName" to record.machineName,
                "output" to record.output,
                "target" to record.target,
                "operatorName" to record.operatorName,
                "notes" to record.notes,
                "syncedAt" to System.currentTimeMillis()
            )
            batch.set(docRef, recordMap, SetOptions.merge())
        }

        batch.commit()
            .addOnSuccessListener {
                _lastSyncTimestamp.value = System.currentTimeMillis()
                Log.d(TAG, "Batch synced ${records.size} production records to Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Batch queued in local cache: ${e.message}")
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
            .set(ticketMap, SetOptions.merge())
            .addOnSuccessListener {
                _lastSyncTimestamp.value = System.currentTimeMillis()
                Log.d(TAG, "Successfully synced service ticket ${ticket.ticketNumber} to Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed or cached offline service ticket: ${e.message}")
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
            .set(attendanceMap, SetOptions.merge())
            .addOnSuccessListener {
                _lastSyncTimestamp.value = System.currentTimeMillis()
                Log.d(TAG, "Successfully synced attendance ${attendance.id} to Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Attendance cached offline: ${e.message}")
            }
    }
}

