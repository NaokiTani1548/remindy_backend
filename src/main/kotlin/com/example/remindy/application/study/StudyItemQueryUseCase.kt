package com.example.remindy.application.study

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StudyItemQueryUseCase(
    private val studyItemRepository: StudyItemRepository,
) {
    fun list(userId: UserId): List<StudyItem> =
        studyItemRepository.findByUserId(userId)

    fun get(userId: UserId, id: StudyItemId): StudyItem =
        studyItemRepository.findById(id)
            ?.takeIf { it.userId == userId }
            ?: throw StudyItemNotFoundException(id)
}
