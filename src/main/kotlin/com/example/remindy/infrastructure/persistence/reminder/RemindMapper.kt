package com.example.remindy.infrastructure.persistence.reminder

import com.example.remindy.domain.reminder.*
import com.example.remindy.domain.shared.UserId
import java.time.*

/**
 * ドメイン Reminder ⇔ 永続化 ReminderRecord の変換。
 * 直和型の平坦化/復元をここに閉じ込め、ドメインにも SpringData にも漏らさない。
 */
object ReminderMapper {

    fun toRecord(reminder: Reminder, clock: Clock): ReminderRecord {
        val now = Instant.now(clock)
        val s = reminder.schedule
        return ReminderRecord(
            id = reminder.id?.value,
            userId = reminder.userId.value,
            title = reminder.title.value,
            scheduleType = scheduleTypeOf(s),
            scheduleTime = s.time,
            scheduleDate = (s as? Schedule.OneTime)?.date,
            scheduleDayOfWeek = (s as? Schedule.Weekly)?.dayOfWeek?.name,
            scheduleDayOfMonth = (s as? Schedule.Monthly)?.dayOfMonth?.value?.toShort(),
            enabled = reminder.enabled,
            createdAt = now,
            updatedAt = now,
        )
    }

    fun toDomain(record: ReminderRecord): Reminder =
        Reminder.reconstitute(
            id = ReminderId(record.id!!),
            userId = UserId(record.userId),
            title = ReminderTitle.of(record.title),
            schedule = scheduleOf(record),
            enabled = record.enabled,
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