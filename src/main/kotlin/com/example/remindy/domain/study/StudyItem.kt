package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId

class StudyItem private constructor(
    val id: StudyItemId?,
    val userId: UserId,
    val kind: StudyItemKind,
    val prompt: Prompt,
    val answer: Answer,
    val enabled: Boolean,
) {
    val isPersisted: Boolean get() = id != null

    companion object {
        fun create(userId: UserId, kind: StudyItemKind, prompt: Prompt, answer: Answer): StudyItem =
            StudyItem(null, userId, kind, prompt, answer, enabled = true)

        fun reconstitute(
            id: StudyItemId, userId: UserId, kind: StudyItemKind,
            prompt: Prompt, answer: Answer, enabled: Boolean,
        ): StudyItem = StudyItem(id, userId, kind, prompt, answer, enabled)
    }
    fun changeContent(kind: StudyItemKind, prompt: Prompt, answer: Answer): StudyItem =
        StudyItem(id, userId, kind, prompt, answer, enabled)

    fun enable(): StudyItem = if (enabled) this else StudyItem(id, userId, kind, prompt, answer, true)
    fun disable(): StudyItem = if (!enabled) this else StudyItem(id, userId, kind, prompt, answer, false)
}