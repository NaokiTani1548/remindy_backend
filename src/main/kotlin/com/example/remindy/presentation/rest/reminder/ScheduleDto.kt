package com.example.remindy.presentation.rest.reminder

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.example.remindy.domain.reminder.*
import java.time.*

/**
 * schedule の JSON 表現。type で分岐する判別共用体を Jackson の多相デシリアライズで表現。
 * ドメインの Schedule(sealed) に対応するが別物。JSONの都合(型タグ・文字列)はここに閉じる。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(ScheduleDto.OneTime::class, name = "ONE_TIME"),
    JsonSubTypes.Type(ScheduleDto.Daily::class,   name = "DAILY"),
    JsonSubTypes.Type(ScheduleDto.Weekly::class,  name = "WEEKLY"),
    JsonSubTypes.Type(ScheduleDto.Monthly::class, name = "MONTHLY"),
)
sealed class ScheduleDto {
    abstract fun toDomain(): Schedule

    data class OneTime(val date: LocalDate, val time: LocalTime) : ScheduleDto() {
        override fun toDomain() = Schedule.OneTime(date, time)
    }
    data class Daily(val time: LocalTime) : ScheduleDto() {
        override fun toDomain() = Schedule.Daily(time)
    }
    data class Weekly(val dayOfWeek: DayOfWeek, val time: LocalTime) : ScheduleDto() {
        override fun toDomain() = Schedule.Weekly(dayOfWeek, time)
    }
    data class Monthly(val dayOfMonth: Int, val time: LocalTime) : ScheduleDto() {
        override fun toDomain() = Schedule.Monthly(DayOfMonth.of(dayOfMonth), time)
    }

    companion object {
        fun fromDomain(s: Schedule): ScheduleDto = when (s) {   // 応答用: ドメイン→DTO
            is Schedule.OneTime -> OneTime(s.date, s.time)
            is Schedule.Daily   -> Daily(s.time)
            is Schedule.Weekly  -> Weekly(s.dayOfWeek, s.time)
            is Schedule.Monthly -> Monthly(s.dayOfMonth.value, s.time)
        }
    }
}