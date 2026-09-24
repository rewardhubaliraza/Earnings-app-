package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AdminLoginScreen
import com.example.ui.AdminScreen
import com.example.ui.CurrentScreen
import com.example.ui.MainViewModel
import com.example.ui.UserHomeScreen
import com.example.ui.UserLoginScreen
import com.example.ui.UserRegisterScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()
        val currentUser by viewModel.currentUser.collectAsState()
        val uiMessage by viewModel.uiMessage.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(uiMessage) {
          uiMessage?.let {
            snackbarHostState.showSnackbar(
              message = it.message,
              duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
          }
        }

        // Handle hardware back press gracefully
        BackHandler(enabled = currentScreen != CurrentScreen.Login) {
          when (currentScreen) {
            CurrentScreen.Register -> viewModel.navigateTo(CurrentScreen.Login)
            CurrentScreen.AdminLogin -> viewModel.navigateTo(CurrentScreen.Login)
            CurrentScreen.AdminHome -> viewModel.logoutAdmin()
            CurrentScreen.UserHome -> viewModel.logoutUser()
            else -> Unit
          }
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (currentScreen) {
              CurrentScreen.Login -> {
                UserLoginScreen(
                  onLoginClick = { phone, pass -> viewModel.loginUser(phone, pass) },
                  onNavigateToRegister = { viewModel.navigateTo(CurrentScreen.Register) },
                  onOpenAdminPanel = { viewModel.navigateTo(CurrentScreen.AdminLogin) }
                )
              }
              CurrentScreen.Register -> {
                UserRegisterScreen(
                  onRegisterClick = { name, phone, pass, email ->
                    viewModel.registerUser(name, phone, pass, email)
                  },
                  onNavigateToLogin = { viewModel.navigateTo(CurrentScreen.Login) },
                  onOpenAdminPanel = { viewModel.navigateTo(CurrentScreen.AdminLogin) }
                )
              }
              CurrentScreen.AdminLogin -> {
                AdminLoginScreen(
                  onLoginClick = { pass -> viewModel.loginAdmin(pass) },
                  onBackToUserApp = { viewModel.navigateTo(CurrentScreen.Login) }
                )
              }
              CurrentScreen.AdminHome -> {
                AdminScreen(
                  viewModel = viewModel,
                  onLogout = { viewModel.logoutAdmin() }
                )
              }
              CurrentScreen.UserHome -> {
                if (currentUser != null) {
                  UserHomeScreen(
                    viewModel = viewModel,
                    user = currentUser!!,
                    onLogout = { viewModel.logoutUser() },
                    onOpenAdminPanel = { viewModel.navigateTo(CurrentScreen.AdminLogin) }
                  )
                } else {
                  // Fallback to login if state was lost
                  LaunchedEffect(Unit) {
                    viewModel.navigateTo(CurrentScreen.Login)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
