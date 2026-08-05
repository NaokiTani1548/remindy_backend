package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId
import java.time.Instant

class StudyItem private constructor(
    val id: StudyItemId?,
    val userId: UserId,
    val kind: StudyItemKind,
    val prompt: Prompt,
    val answer: Answer,
    val enabled: Boolean,
    val deletedAt: Instant? = null,
) {
    val isPersisted: Boolean get() = id != null
    val isDeleted: Boolean get() = deletedAt != null

    companion object {
        fun create(userId: UserId, kind: StudyItemKind, prompt: Prompt, answer: Answer): StudyItem =
            StudyItem(null, userId, kind, prompt, answer, enabled = true)

        fun reconstitute(
            id: StudyItemId, userId: UserId, kind: StudyItemKind,
            prompt: Prompt, answer: Answer, enabled: Boolean,
            deletedAt: Instant? = null,
        ): StudyItem = StudyItem(id, userId, kind, prompt, answer, enabled, deletedAt)
    }

    fun changeContent(kind: StudyItemKind, prompt: Prompt, answer: Answer): StudyItem =
        StudyItem(id, userId, kind, prompt, answer, enabled, deletedAt)

    fun enable(): StudyItem = if (enabled) this else StudyItem(id, userId, kind, prompt, answer, true, deletedAt)
    fun disable(): StudyItem = if (!enabled) this else StudyItem(id, userId, kind, prompt, answer, false, deletedAt)

    fun softDelete(now: Instant): StudyItem =
        if (isDeleted) this else StudyItem(id, userId, kind, prompt, answer, enabled, now)
}