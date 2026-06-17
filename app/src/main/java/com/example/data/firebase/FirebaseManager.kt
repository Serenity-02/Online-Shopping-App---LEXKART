package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    var isInitialized = false
        private set

    var firestore: FirebaseFirestore? = null
        private set

    fun initialize(context: Context) {
        try {
            // Attempt to initialize standard Firebase.
            // Under normal circumstances with google-services.json available, this initializes smoothly.
            val app = FirebaseApp.initializeApp(context)
            if (app != null) {
                firestore = FirebaseFirestore.getInstance()
                isInitialized = true
                Log.d(TAG, "Firebase successfully initialized in LEXKART!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase configuration initialization failed: ${e.message}. Falling back to default SQLite/Room storage.", e)
            isInitialized = false
            firestore = null
        }
    }
}
