package com.example.remindy.application.reminder.intake

import java.time.OffsetDateTime

class ReminderParseException(message: String) : RuntimeException(message)

interface ReminderParser {
    fun parse(text: String, referenceTime: OffsetDateTime): ReminderDraft
}
