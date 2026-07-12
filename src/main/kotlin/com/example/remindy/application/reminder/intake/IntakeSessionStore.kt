package com.example.remindy.application.reminder.intake

import java.util.UUID

class IntakeSessionNotFoundException(id: UUID) :
    RuntimeException("Intake session not found or expired: $id")

interface IntakeSessionStore {
    fun save(session: IntakeSession): IntakeSession
    fun find(id: UUID): IntakeSession?
    fun delete(id: UUID)
}
