package com.example.remindy.infrastructure.persistence.study

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.Frequency
import com.example.remindy.domain.study.StudyNotificationSetting
import com.example.remindy.domain.study.StudyNotificationSettingRepository
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.Instant

@Repository
class StudyNotificationSettingRepositoryImpl(
    private val jdbc: StudyNotificationSettingJdbcRepository,
    private val clock: Clock,
) : StudyNotificationSettingRepository {

    override fun save(setting: StudyNotificationSetting): StudyNotificationSetting {
        val exists = jdbc.existsById(setting.userId.value)
        val record = StudyNotificationSettingRecord(
            userId = setting.userId.value,
            frequency = setting.frequency.name,
            enabled = setting.enabled,
            updatedAt = Instant.now(clock),
            newRecord = !exists,
        )
        return toDomain(jdbc.save(record))
    }

    override fun findByUserId(userId: UserId): StudyNotificationSetting? =
        jdbc.findById(userId.value).map(::toDomain).orElse(null)

    override fun findByUserIdIfModifiedSince(userId: UserId, since: Instant): StudyNotificationSetting? =
        jdbc.findByUserIdAndUpdatedAtGreaterThan(userId.value, since)?.let(::toDomain)

    private fun toDomain(record: StudyNotificationSettingRecord): StudyNotificationSetting =
        StudyNotificationSetting.reconstitute(
            userId = UserId(record.userId),
            frequency = Frequency.valueOf(record.frequency),
            enabled = record.enabled,
        )
}
