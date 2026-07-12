package com.example.remindy.application.study.command

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.StudyItemId
import com.example.remindy.domain.study.StudyItemKind

/**
 * 学習項目の内容を置換するコマンド。PUT /study-items/{id} に対応。
 * prompt と answer はどちらも String のため、名前付きフィールドで受けることで
 * 引数の取り違え(表と裏の逆転)を構造的に防いでいる。
 */
data class UpdateStudyItemCommand(
    val userId: UserId,
    val id: StudyItemId,
    val kind: StudyItemKind,
    val prompt: String,
    val answer: String,
)