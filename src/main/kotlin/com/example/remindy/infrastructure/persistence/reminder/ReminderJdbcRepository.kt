package com.example.remindy.infrastructure.persistence.reminder

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.util.UUID

interface ReminderJdbcRepository : CrudRepository<ReminderRecord, UUID> {
    fun findByUserId(userId: UUID): List<ReminderRecord>
    fun findByUserIdAndDeletedAtIsNull(userId: UUID): List<ReminderRecord>
    fun findByUserIdAndUpdatedAtGreaterThan(userId: UUID, updatedAt: Instant): List<ReminderRecord>

    @Modifying
    @Query("UPDATE reminders SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    fun softDelete(id: UUID, deletedAt: Instant, updatedAt: Instant)

    @Query("""
        INSERT INTO reminders (id, user_id, title, schedule_type, schedule_time, schedule_date,
            schedule_day_of_week, schedule_day_of_month, enabled, created_at, updated_at, deleted_at)
        VALUES (:#{#r.id}, :#{#r.userId}, :#{#r.title}, :#{#r.scheduleType}, :#{#r.scheduleTime},
            :#{#r.scheduleDate}, :#{#r.scheduleDayOfWeek}, :#{#r.scheduleDayOfMonth},
            :#{#r.enabled}, :#{#r.createdAt}, :#{#r.updatedAt}, :#{#r.deletedAt})
        ON CONFLICT (id) DO UPDATE SET
            title = EXCLUDED.title, schedule_type = EXCLUDED.schedule_type,
            schedule_time = EXCLUDED.schedule_time, schedule_date = EXCLUDED.schedule_date,
            schedule_day_of_week = EXCLUDED.schedule_day_of_week,
            schedule_day_of_month = EXCLUDED.schedule_day_of_month,
            enabled = EXCLUDED.enabled, updated_at = EXCLUDED.updated_at,
            deleted_at = EXCLUDED.deleted_at
    """)
    fun upsert(r: ReminderRecord)
}