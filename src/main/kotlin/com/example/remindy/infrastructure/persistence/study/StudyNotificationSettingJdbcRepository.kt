package com.example.remindy.infrastructure.persistence.study

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.util.UUID

interface StudyNotificationSettingJdbcRepository : CrudRepository<StudyNotificationSettingRecord, UUID> {
    @Query("SELECT * FROM study_notification_settings WHERE user_id = :userId AND updated_at > :since")
    fun findByUserIdAndUpdatedAtGreaterThan(userId: UUID, since: Instant): StudyNotificationSettingRecord?
}
