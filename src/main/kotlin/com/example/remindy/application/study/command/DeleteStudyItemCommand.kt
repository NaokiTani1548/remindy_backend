package com.example.remindy.application.study.command

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.StudyItemId

/** 削除コマンド。DELETE /study-items/{id} に対応。 */
data class DeleteStudyItemCommand(
    val userId: UserId,
    val id: StudyItemId,
)