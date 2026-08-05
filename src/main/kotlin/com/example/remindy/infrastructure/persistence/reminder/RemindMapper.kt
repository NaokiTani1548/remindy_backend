package com.example.remindy.infrastructure.persistence.reminder

import com.example.remindy.domain.reminder.*
import com.example.remindy.domain.shared.UserId
import java.time.*
import java.util.UUID

/**
 * ドメイン Reminder ⇔ 永続化 ReminderRecord の変換。
 * 直和型の平坦化/復元をここに閉じ込め、ドメインにも SpringData にも漏らさない。
 */
object ReminderMapper {

    fun toNewRecord(reminder: Reminder, now: Instant): ReminderRecord =
        buildRecord(reminder, id = null, createdAt = now, updatedAt = now)

    fun toExistingRecord(reminder: Reminder, createdAt: Instant, updatedAt: Instant): ReminderRecord =
        buildRecord(reminder, id = reminder.id!!.value, createdAt = createdAt, updatedAt = updatedAt)

    private fun buildRecord(
        reminder: Reminder,
        id: UUID?,
        createdAt: Instant,
        updatedAt: Instant,
    ): ReminderRecord {
        val s = reminder.schedule
        return ReminderRecord(
            id = id,
            userId = reminder.userId.value,
            title = reminder.title.value,
            scheduleType = scheduleTypeOf(s),
            scheduleTime = s.time,
            scheduleDate = (s as? Schedule.OneTime)?.date,
            scheduleDayOfWeek = (s as? Schedule.Weekly)?.dayOfWeek?.name,
            scheduleDayOfMonth = (s as? Schedule.Monthly)?.dayOfMonth?.value?.toShort(),
            enabled = reminder.enabled,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = reminder.deletedAt,
        )
    }

    fun toDomain(record: ReminderRecord): Reminder =
        Reminder.reconstitute(
            id = ReminderId(record.id!!),
            userId = UserId(record.userId),
            title = ReminderTitle.of(record.title),
            schedule = scheduleOf(record),
            enabled = record.enabled,
            deletedAt = record.deletedAt,
        )

    private fun scheduleTypeOf(s: Schedule): String = when (s) {
        is Schedule.OneTime -> "ONE_TIME"
        is Schedule.Daily   -> "DAILY"
        is Schedule.Weekly  -> "WEEKLY"
        is Schedule.Monthly -> "MONTHLY"
    }

    private fun scheduleOf(r: ReminderRecord): Schedule = when (r.scheduleType) {
        "ONE_TIME" -> Schedule.OneTime(r.scheduleDate!!, r.scheduleTime)
        "DAILY"    -> Schedule.Daily(r.scheduleTime)
        "WEEKLY"   -> Schedule.Weekly(DayOfWeek.valueOf(r.scheduleDayOfWeek!!), r.scheduleTime)
        "MONTHLY"  -> Schedule.Monthly(DayOfMonth.of(r.scheduleDayOfMonth!!.toInt()), r.scheduleTime)
        else -> error("未知の scheduleType: ${r.scheduleType}")
    }
}