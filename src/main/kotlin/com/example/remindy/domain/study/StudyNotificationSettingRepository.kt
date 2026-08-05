package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId
import java.time.Instant

interface StudyNotificationSettingRepository {
    fun save(setting: StudyNotificationSetting): StudyNotificationSetting
    fun findByUserId(userId: UserId): StudyNotificationSetting?
    fun findByUserIdIfModifiedSince(userId: UserId, since: Instant): StudyNotificationSetting?
}
