package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId

interface StudyNotificationSettingRepository {
    fun save(setting: StudyNotificationSetting): StudyNotificationSetting
    fun findByUserId(userId: UserId): StudyNotificationSetting?
}
