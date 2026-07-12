package com.example.remindy.application.study.command

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.StudyItemKind

data class CreateStudyItemCommand (
    val userId: UserId,
    val kind: StudyItemKind,
    val prompt: String,
    val answer: String,
)