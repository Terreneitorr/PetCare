package com.tuapp.petcare.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tuapp.petcare.features.auth.presentation.screens.LoginScreen
import com.tuapp.petcare.features.auth.presentation.screens.RegisterScreen
import com.tuapp.petcare.features.medical.presentation.screens.AddVaccineScreen
import com.tuapp.petcare.features.medical.presentation.screens.MedHistoryScreen
import com.tuapp.petcare.features.medical.presentation.screens.QrScannerScreen
import com.tuapp.petcare.features.medical.presentation.viewmodels.MedHistoryViewModel
import com.tuapp.petcare.features.pets.presentation.screens.AddPetScreen
import com.tuapp.petcare.features.pets.presentation.screens.PetListScreen
import com.tuapp.petcare.features.profile.presentation.screens.EditProfileScreen
import com.tuapp.petcare.features.profile.presentation.screens.ProfileScreen
import com.tuapp.petcare.features.reminders.presentation.screens.RemindersScreen
import kotlinx.serialization.Serializable

// ── Rutas tipadas ─────────────────────────────────────────────────────────────
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object PetListRoute
@Serializable object AddPetRoute
@Serializable data class MedHistoryRoute(val petId: String)
@Serializable data class AddVaccineRoute(val petId: String)
@Serializable data class QrScannerRoute(val petId: String)
@Serializable object RemindersRoute
@Serializable object ProfileRoute
@Serializable data class EditProfileRoute(val userId: String, val email: String)

// ── NavHost principal ─────────────────────────────────────────────────────────
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = LoginRoute
    ) {

        // AUTH
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(PetListRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(RegisterRoute) }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(PetListRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        // PETS — F01
        composable<PetListRoute> {
            PetListScreen(
                onAddPet = { navController.navigate(AddPetRoute) },
                onPetClick = { petId ->
                    navController.navigate(MedHistoryRoute(petId))
                },
                onReminders = { navController.navigate(RemindersRoute) },
                onProfile = { navController.navigate(ProfileRoute) }
            )
        }

        composable<AddPetRoute> {
            AddPetScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // MEDICAL — F02
        composable<MedHistoryRoute> { backStackEntry ->
            val route: MedHistoryRoute = backStackEntry.toRoute()
            MedHistoryScreen(
                petId = route.petId,
                onBack = { navController.popBackStack() },
                onAddVaccine = { navController.navigate(AddVaccineRoute(route.petId)) }
            )
        }

        composable<AddVaccineRoute> { backStackEntry ->
            val route: AddVaccineRoute = backStackEntry.toRoute()
            val viewModel = hiltViewModel<MedHistoryViewModel>(backStackEntry)
            AddVaccineScreen(
                petId = route.petId,
                onBack = { navController.popBackStack() },
                onScanQr = { navController.navigate(QrScannerRoute(route.petId)) },
                viewModel = viewModel
            )
        }

        composable<QrScannerRoute> { backStackEntry ->
            val route: QrScannerRoute = backStackEntry.toRoute()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<AddVaccineRoute>()
            }
            val viewModel = hiltViewModel<MedHistoryViewModel>(parentEntry)
            QrScannerScreen(
                petId = route.petId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // REMINDERS — F03
        composable<RemindersRoute> {
            RemindersScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // PROFILE — MVP1
        composable<ProfileRoute> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = {
                    // Por ahora usamos valores vacíos
                    // En producción vendrían del DataStore
                    navController.navigate(EditProfileRoute("local_user", ""))
                }
            )
        }

        composable<EditProfileRoute> { backStackEntry ->
            val route: EditProfileRoute = backStackEntry.toRoute()
            EditProfileScreen(
                userId = route.userId,
                email = route.email,
                onBack = { navController.popBackStack() }
            )
        }
    }
}