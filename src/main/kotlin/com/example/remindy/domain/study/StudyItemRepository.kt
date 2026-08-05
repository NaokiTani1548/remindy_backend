package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId
import java.time.Instant

interface StudyItemRepository {
    fun save(studyItem: StudyItem): StudyItem
    fun findById(id: StudyItemId): StudyItem?
    fun findByUserId(userId: UserId): List<StudyItem>
    fun deleteById(id: StudyItemId)

    fun findByUserIdModifiedSince(userId: UserId, since: Instant): List<StudyItem>
    fun upsert(studyItem: StudyItem, createdAt: Instant, updatedAt: Instant)
    fun softDelete(id: StudyItemId, deletedAt: Instant)
}
