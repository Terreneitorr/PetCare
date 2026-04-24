package com.tuapp.petcare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tuapp.petcare.core.navigation.AppNavigation
import com.tuapp.petcare.core.ui.theme.PetCareTheme
import com.tuapp.petcare.core.workers.NotificationHelper
import com.tuapp.petcare.features.auth.domain.entities.UserRole
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) NotificationHelper.createChannels(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannels(this)
        requestNotificationPermission()

        lifecycleScope.launch {
            val token    = authRepository.getToken()
            val role     = authRepository.getRole()
            val isLoggedIn = !token.isNullOrBlank()

            setContent {
                PetCareTheme {
                    AppNavigation(
                        isLoggedIn = isLoggedIn,
                        userRole   = role
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationHelper.createChannels(this)
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}