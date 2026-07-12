package com.example.remindy.application.study

import com.example.remindy.application.study.command.CreateStudyItemCommand
import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateStudyItemUseCase(
    private val studyItemRepository: StudyItemRepository,
) {
    @Transactional
    fun handle(command: CreateStudyItemCommand): StudyItem {
        val item = StudyItem.create(
            userId = command.userId,
            kind = command.kind,
            prompt = Prompt.of(command.prompt),
            answer = Answer.of(command.answer),
        )
        return studyItemRepository.save(item)
    }
}
