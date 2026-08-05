package com.example.remindy.infrastructure.persistence.study

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.StudyItem
import com.example.remindy.domain.study.StudyItemId
import com.example.remindy.domain.study.StudyItemNotFoundException
import com.example.remindy.domain.study.StudyItemRepository
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.Instant

@Repository
class StudyItemRepositoryImpl(
    private val jdbc: StudyItemJdbcRepository,
    private val clock: Clock,
) : StudyItemRepository {

    override fun save(studyItem: StudyItem): StudyItem {
        val now = Instant.now(clock)
        val record = if (studyItem.id == null) {
            StudyItemMapper.toNewRecord(studyItem, now)
        } else {
            val createdAt = jdbc.findById(studyItem.id!!.value)
                .map { it.createdAt }
                .orElseThrow { StudyItemNotFoundException(studyItem.id!!) }
            StudyItemMapper.toExistingRecord(studyItem, createdAt = createdAt, updatedAt = now)
        }
        return StudyItemMapper.toDomain(jdbc.save(record))
    }

    override fun findById(id: StudyItemId): StudyItem? =
        jdbc.findById(id.value).map(StudyItemMapper::toDomain).orElse(null)

    override fun findByUserId(userId: UserId): List<StudyItem> =
        jdbc.findByUserIdAndDeletedAtIsNull(userId.value).map(StudyItemMapper::toDomain)

    override fun deleteById(id: StudyItemId) {
        val now = Instant.now(clock)
        jdbc.softDelete(id.value, deletedAt = now, updatedAt = now)
    }

    override fun findByUserIdModifiedSince(userId: UserId, since: Instant): List<StudyItem> =
        jdbc.findByUserIdAndUpdatedAtGreaterThan(userId.value, since).map(StudyItemMapper::toDomain)

    override fun upsert(studyItem: StudyItem, createdAt: Instant, updatedAt: Instant) {
        val record = StudyItemMapper.toExistingRecord(studyItem, createdAt = createdAt, updatedAt = updatedAt)
        jdbc.upsert(record)
    }

    override fun softDelete(id: StudyItemId, deletedAt: Instant) {
        jdbc.softDelete(id.value, deletedAt = deletedAt, updatedAt = deletedAt)
    }
}
