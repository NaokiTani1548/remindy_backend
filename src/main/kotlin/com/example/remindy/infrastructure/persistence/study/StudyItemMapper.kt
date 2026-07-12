package com.example.remindy.infrastructure.persistence.study

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.*
import java.time.Instant
import java.util.UUID

object StudyItemMapper {
    fun toNewRecord(item: StudyItem, now: Instant): StudyItemRecord =
        build(item, id = null, createdAt = now, updatedAt = now)

    fun toExistingRecord(item: StudyItem, createdAt: Instant, updatedAt: Instant): StudyItemRecord =
        build(item, id = item.id!!.value, createdAt = createdAt, updatedAt = updatedAt)

    private fun build(item: StudyItem, id: UUID?, createdAt: Instant, updatedAt: Instant): StudyItemRecord =
        StudyItemRecord(
            id = id,
            userId = item.userId.value,
            kind = item.kind.name,
            prompt = item.prompt.value,
            answer = item.answer.value,
            enabled = item.enabled,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun toDomain(record: StudyItemRecord): StudyItem =
        StudyItem.reconstitute(
            id = StudyItemId(record.id!!),
            userId = UserId(record.userId),
            kind = StudyItemKind.valueOf(record.kind),
            prompt = Prompt.of(record.prompt),
            answer = Answer.of(record.answer),
            enabled = record.enabled,
        )
}
