package com.example.remindy.domain.reminder

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * 通知タイミング。4種の直和型(sealed)。
 *
 * 設計の肝: 各バリアントは「自分に必要なフィールドだけ」を持つ。
 * これにより DAILY なのに曜日を持つ、WEEKLY なのに日にちを持つ、といった
 * 不正な状態が"型として表現不可能"になる。DB の ck_reminders_schedule_shape が
 * 行レベルで守るのと同じことを、コンパイル時に守っている。
 *
 * time は全バリアント共通なので interface のプロパティに引き上げている。
 * 曜日は java.time.DayOfWeek(MONDAY..SUNDAY)をそのまま採用。DB の varchar 値と
 * 綴りが一致するので、永続化層では .name で素直にマッピングできる(が、それは infra の仕事)。
 */
sealed interface Schedule {
    val time: LocalTime

    /** 一回限り: 日付 + 時刻 */
    data class OneTime(val date: LocalDate, override val time: LocalTime) : Schedule

    /** 毎日: 時刻のみ */
    data class Daily(override val time: LocalTime) : Schedule

    /** 毎週: 曜日 + 時刻 */
    data class Weekly(val dayOfWeek: DayOfWeek, override val time: LocalTime) : Schedule

    /** 毎月: 日にち + 時刻 */
    data class Monthly(val dayOfMonth: DayOfMonth, override val time: LocalTime) : Schedule
}