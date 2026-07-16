package com.example.remindy.infrastructure.persistence.study

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface StudyItemJdbcRepository : CrudRepository<StudyItemRecord, UUID> {
    fun findByUserId(userId: UUID): List<StudyItemRecord>
}
