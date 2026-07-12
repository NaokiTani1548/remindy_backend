package com.example.remindy.infrastructure.llm

import com.example.remindy.application.reminder.intake.IntakeSession
import com.example.remindy.application.reminder.intake.IntakeSessionStore
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryIntakeSessionStore(
    private val clock: Clock,
    private val properties: IntakeProperties,
) : IntakeSessionStore {

    private val sessions = ConcurrentHashMap<UUID, IntakeSession>()

    override fun save(session: IntakeSession): IntakeSession {
        sessions[session.id] = session
        return session
    }

    override fun find(id: UUID): IntakeSession? {
        val session = sessions[id] ?: return null
        if (isExpired(session.createdAt)) {
            sessions.remove(id)
            return null
        }
        return session
    }

    override fun delete(id: UUID) {
        sessions.remove(id)
    }

    private fun isExpired(createdAt: Instant): Boolean =
        Instant.now(clock).isAfter(createdAt.plusSeconds(properties.sessionTtlSeconds))
}
