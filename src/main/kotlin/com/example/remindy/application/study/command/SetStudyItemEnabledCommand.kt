package com.example.remindy.application.study.command

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.StudyItemId

/** 有効/無効の切替コマンド。PATCH /study-items/{id} に対応。 */
data class SetStudyItemEnabledCommand(
    val userId: UserId,
    val id: StudyItemId,
    val enabled: Boolean,
)