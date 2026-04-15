package com.tuapp.petcare.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tuapp.petcare.features.appointments.presentation.screens.AddAppointmentScreen
import com.tuapp.petcare.features.appointments.presentation.screens.AppointmentsScreen
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
import com.tuapp.petcare.features.weight.presentation.screens.AddWeightScreen
import com.tuapp.petcare.features.weight.presentation.screens.WeightScreen
import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object PetListRoute
@Serializable object AddPetRoute
@Serializable data class MedHistoryRoute(
    val petId: String,
    val petName: String,
    val petSpecies: String,
    val petBirthDate: String
)
@Serializable data class AddVaccineRoute(val petId: String)
@Serializable data class QrScannerRoute(val petId: String)
@Serializable object RemindersRoute
@Serializable object ProfileRoute
@Serializable data class EditProfileRoute(val userId: String, val email: String)
@Serializable data class AppointmentsRoute(val petId: String, val petName: String)
@Serializable data class AddAppointmentRoute(val petId: String, val petName: String)
@Serializable data class WeightRoute(
    val petId: String,
    val petName: String,
    val petSpecies: String,
    val petBirthDate: String
)
@Serializable data class AddWeightRoute(val petId: String, val petName: String)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    isLoggedIn: Boolean = false   // ← nuevo parámetro
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) PetListRoute else LoginRoute  // ← dinámico
    ) {

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

        composable<PetListRoute> {
            PetListScreen(
                onAddPet = { navController.navigate(AddPetRoute) },
                onPetClick = { pet ->
                    navController.navigate(
                        MedHistoryRoute(
                            petId = pet.id,
                            petName = pet.name,
                            petSpecies = pet.species,
                            petBirthDate = pet.birthDate
                        )
                    )
                },
                onReminders = { navController.navigate(RemindersRoute) },
                onProfile = { navController.navigate(ProfileRoute) }
            )
        }

        composable<AddPetRoute> {
            AddPetScreen(onBack = { navController.popBackStack() })
        }

        composable<MedHistoryRoute> { backStackEntry ->
            val route: MedHistoryRoute = backStackEntry.toRoute()
            MedHistoryScreen(
                petId = route.petId,
                onBack = { navController.popBackStack() },
                onAddVaccine = { navController.navigate(AddVaccineRoute(route.petId)) },
                onAppointments = {
                    navController.navigate(AppointmentsRoute(route.petId, route.petName))
                },
                onWeightGrowth = {
                    navController.navigate(
                        WeightRoute(
                            petId = route.petId,
                            petName = route.petName,
                            petSpecies = route.petSpecies,
                            petBirthDate = route.petBirthDate
                        )
                    )
                }
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

        composable<RemindersRoute> {
            RemindersScreen(onBack = { navController.popBackStack() })
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = {
                    navController.navigate(EditProfileRoute("local_user", ""))
                },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
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

        composable<AppointmentsRoute> { backStackEntry ->
            val route: AppointmentsRoute = backStackEntry.toRoute()
            AppointmentsScreen(
                petId = route.petId,
                petName = route.petName,
                onBack = { navController.popBackStack() },
                onAddAppointment = {
                    navController.navigate(AddAppointmentRoute(route.petId, route.petName))
                }
            )
        }

        composable<AddAppointmentRoute> { backStackEntry ->
            val route: AddAppointmentRoute = backStackEntry.toRoute()
            AddAppointmentScreen(
                petId = route.petId,
                petName = route.petName,
                onBack = { navController.popBackStack() }
            )
        }

        composable<WeightRoute> { backStackEntry ->
            val route: WeightRoute = backStackEntry.toRoute()
            WeightScreen(
                petId = route.petId,
                petName = route.petName,
                petSpecies = route.petSpecies,
                petBirthDate = route.petBirthDate,
                onBack = { navController.popBackStack() },
                onAddWeight = {
                    navController.navigate(AddWeightRoute(route.petId, route.petName))
                }
            )
        }

        composable<AddWeightRoute> { backStackEntry ->
            val route: AddWeightRoute = backStackEntry.toRoute()
            AddWeightScreen(
                petId = route.petId,
                petName = route.petName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}