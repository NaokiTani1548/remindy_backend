package com.example.remindy.infrastructure.persistence.reminder

import com.example.remindy.domain.reminder.*
import com.example.remindy.domain.shared.UserId
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.Instant

@Repository
class ReminderRepositoryImpl(
    private val jdbc: ReminderJdbcRepository,
    private val clock: Clock,
) : ReminderRepository {

    override fun save(reminder: Reminder): Reminder {
        val now = Instant.now(clock)
        val record = if (reminder.id == null) {
            ReminderMapper.toNewRecord(reminder, now)
        } else {
            val createdAt = jdbc.findById(reminder.id!!.value)
                .map { it.createdAt }
                .orElseThrow { ReminderNotFoundException(reminder.id!!) }
            ReminderMapper.toExistingRecord(reminder, createdAt = createdAt, updatedAt = now)
        }
        return ReminderMapper.toDomain(jdbc.save(record))
    }

    override fun findById(id: ReminderId): Reminder? =
        jdbc.findById(id.value).map(ReminderMapper::toDomain).orElse(null)

    override fun findByUserId(userId: UserId): List<Reminder> =
        jdbc.findByUserIdAndDeletedAtIsNull(userId.value).map(ReminderMapper::toDomain)

    override fun deleteById(id: ReminderId) {
        val now = Instant.now(clock)
        jdbc.softDelete(id.value, deletedAt = now, updatedAt = now)
    }

    override fun findByUserIdModifiedSince(userId: UserId, since: Instant): List<Reminder> =
        jdbc.findByUserIdAndUpdatedAtGreaterThan(userId.value, since).map(ReminderMapper::toDomain)

    override fun upsert(reminder: Reminder, createdAt: Instant, updatedAt: Instant) {
        val record = ReminderMapper.toExistingRecord(reminder, createdAt = createdAt, updatedAt = updatedAt)
        jdbc.upsert(record)
    }

    override fun softDelete(id: ReminderId, deletedAt: Instant) {
        jdbc.softDelete(id.value, deletedAt = deletedAt, updatedAt = deletedAt)
    }
}