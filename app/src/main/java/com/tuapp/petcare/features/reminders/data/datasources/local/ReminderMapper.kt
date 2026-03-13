package com.tuapp.petcare.features.reminders.data.datasources.local

import com.tuapp.petcare.features.reminders.domain.entities.Reminder

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id             = this.id,
    petId          = this.petId,
    petName        = this.petName,
    title          = this.title,
    description    = this.description,
    triggerAtMillis = this.triggerAtMillis,
    isActive       = this.isActive
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id             = this.id,
    petId          = this.petId,
    petName        = this.petName,
    title          = this.title,
    description    = this.description,
    triggerAtMillis = this.triggerAtMillis,
    isActive       = this.isActive
)
