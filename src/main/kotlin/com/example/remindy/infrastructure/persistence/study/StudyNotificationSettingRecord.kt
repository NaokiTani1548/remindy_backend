
package com.example.remindy.infrastructure.persistence.study

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("study_notification_settings")
data class StudyNotificationSettingRecord(
    @Id val userId: UUID,
    val frequency: String,
    val enabled: Boolean,
    val updatedAt: Instant,
    @Transient val newRecord: Boolean = false,
) : Persistable<UUID> {
    override fun getId(): UUID = userId
    override fun isNew(): Boolean = newRecord
}
