package com.example.remindy.infrastructure.persistence.study

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface StudyNotificationSettingJdbcRepository : CrudRepository<StudyNotificationSettingRecord, UUID>
