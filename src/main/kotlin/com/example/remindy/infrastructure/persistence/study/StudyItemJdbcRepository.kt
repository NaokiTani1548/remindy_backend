package com.example.remindy.infrastructure.persistence.study

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.util.UUID

interface StudyItemJdbcRepository : CrudRepository<StudyItemRecord, UUID> {
    fun findByUserId(userId: UUID): List<StudyItemRecord>
    fun findByUserIdAndDeletedAtIsNull(userId: UUID): List<StudyItemRecord>
    fun findByUserIdAndUpdatedAtGreaterThan(userId: UUID, updatedAt: Instant): List<StudyItemRecord>

    @Modifying
    @Query("UPDATE study_items SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    fun softDelete(id: UUID, deletedAt: Instant, updatedAt: Instant)

    @Query("""
        INSERT INTO study_items (id, user_id, kind, prompt, answer, enabled, created_at, updated_at, deleted_at)
        VALUES (:#{#r.id}, :#{#r.userId}, :#{#r.kind}, :#{#r.prompt}, :#{#r.answer},
            :#{#r.enabled}, :#{#r.createdAt}, :#{#r.updatedAt}, :#{#r.deletedAt})
        ON CONFLICT (id) DO UPDATE SET
            kind = EXCLUDED.kind, prompt = EXCLUDED.prompt, answer = EXCLUDED.answer,
            enabled = EXCLUDED.enabled, updated_at = EXCLUDED.updated_at,
            deleted_at = EXCLUDED.deleted_at
    """)
    fun upsert(r: StudyItemRecord)
}
