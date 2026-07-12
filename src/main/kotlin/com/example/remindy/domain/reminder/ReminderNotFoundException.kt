package com.example.remindy.domain.reminder

/**
 * リマインダーが存在しない、または要求者の所有物でないときに投げる。
 * 「他人の資源」と「存在しない」を区別しないことで存在を秘匿する（API仕様Q5=404）。
 */
class ReminderNotFoundException(id: ReminderId) :
    RuntimeException("Reminder not found: ${id.value}")