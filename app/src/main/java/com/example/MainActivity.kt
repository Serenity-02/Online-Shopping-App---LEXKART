package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.firebase.FirebaseManager
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ShoppingAppMain
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShoppingViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Firebase/Firestore. Gracefully falls back to local database if google-services.json isn't present
    FirebaseManager.initialize(applicationContext)

    setContent {
      val viewModel: ShoppingViewModel = viewModel()
      val activeUser by viewModel.currentUser.collectAsState()

      // Dynamically resolve Dark theme based on custom user-profile setting settings
      val resolvedDarkTheme = activeUser?.isDarkMode ?: isSystemInDarkTheme()

      MyApplicationTheme(darkTheme = resolvedDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (activeUser == null) {
            AuthScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          } else {
            ShoppingAppMain(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    }
  }
}
