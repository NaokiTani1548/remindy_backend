package com.example.remindy.infrastructure.persistence.study

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("study_items")
data class StudyItemRecord(
    @Id val id: UUID?,
    val userId: UUID,
    val kind: String,
    val prompt: String,
    val answer: String,
    val enabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
