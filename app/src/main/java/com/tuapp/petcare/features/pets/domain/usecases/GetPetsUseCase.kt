package com.tuapp.petcare.features.pets.domain.usecases

import com.tuapp.petcare.features.pets.domain.entities.Pet
import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetsUseCase @Inject constructor(
    private val repository: PetsRepository
) {
    operator fun invoke(): Flow<List<Pet>> = repository.getPets()
}
