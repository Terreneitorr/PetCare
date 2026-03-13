package com.tuapp.petcare.features.auth.data.datasources.remote.mapper

import com.tuapp.petcare.features.auth.data.datasources.remote.models.AuthResponseDto
import com.tuapp.petcare.features.auth.domain.entities.User

fun AuthResponseDto.toDomain(): User {
    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        token = this.token
    )
}
