package com.tuapp.petcare.features.auth.domain.usecases

import com.tuapp.petcare.features.auth.domain.entities.User
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank())
            return Result.failure(Exception("Todos los campos son obligatorios"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Result.failure(Exception("El email no es válido"))
        if (password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        return repository.register(name, email, password)
    }
}
