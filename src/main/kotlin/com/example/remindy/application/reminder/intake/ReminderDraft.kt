package com.example.remindy.application.reminder.intake

import com.example.remindy.domain.reminder.Schedule

data class ReminderDraft(
    val title: String? = null,
    val schedule: Schedule? = null,
) {
    fun mergedWith(other: ReminderDraft): ReminderDraft =
        ReminderDraft(
            title = other.title ?: title,
            schedule = other.schedule ?: schedule,
        )

    fun missingElements(): List<MissingElement> = buildList {
        if (title.isNullOrBlank()) add(MissingElement.TITLE)
        if (schedule == null) add(MissingElement.SCHEDULE)
    }

    fun status(): IntakeStatus =
        if (missingElements().isEmpty()) IntakeStatus.READY else IntakeStatus.NEEDS_MORE_INFO
}

enum class MissingElement { TITLE, SCHEDULE }

enum class IntakeStatus { READY, NEEDS_MORE_INFO }
