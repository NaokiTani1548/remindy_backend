package com.example.remindy.application.study

import com.example.remindy.application.study.command.DeleteStudyItemCommand
import com.example.remindy.application.study.command.SetStudyItemEnabledCommand
import com.example.remindy.application.study.command.UpdateStudyItemCommand
import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateStudyItemUseCase(
    private val studyItemRepository: StudyItemRepository,
) {
    @Transactional
    fun changeContent(
        command: UpdateStudyItemCommand,
    ): StudyItem {
        val current = load(command.userId, command.id)
        val updated = current.changeContent(command.kind, Prompt.of(command.prompt), Answer.of(command.answer))
        return studyItemRepository.save(updated)
    }

    @Transactional
    fun setEnabled(command: SetStudyItemEnabledCommand): StudyItem {
        val current = load(command.userId, command.id)
        val updated = if (command.enabled) current.enable() else current.disable()
        return studyItemRepository.save(updated)
    }

    @Transactional
    fun delete(command: DeleteStudyItemCommand) {
        load(command.userId, command.id)
        studyItemRepository.deleteById(command.id)
    }

    private fun load(userId: UserId, id: StudyItemId): StudyItem =
        studyItemRepository.findById(id)
            ?.takeIf { it.userId == userId }
            ?: throw StudyItemNotFoundException(id)
}
