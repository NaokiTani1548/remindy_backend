package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId

interface StudyItemRepository {
    fun save(studyItem: StudyItem): StudyItem
    fun findById(id: StudyItemId): StudyItem?
    fun findByUserId(userId: UserId): List<StudyItem>
    fun deleteById(id: StudyItemId)
}
