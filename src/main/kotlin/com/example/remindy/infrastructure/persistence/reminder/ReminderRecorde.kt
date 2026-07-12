package com.example.remindy.infrastructure.persistence.reminder

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.*
import java.util.UUID

/**
 * reminders テーブルの1行に対応する永続化モデル。ドメインの Reminder とは別物。
 * ・SpringData の @Id/@Table はここ(infra)にだけ付く。ドメインは無傷。
 * ・Schedule 直和型を「判別子 scheduleType + nullable 列」に平坦化して保持。
 * ・id は UUID?。null=未永続。SpringDataJDBC が null-id を INSERT と判定し、
 *   DB の DEFAULT uuidv7() で採番された値を保存後に埋めて返す。
 */
@Table("reminders")
data class ReminderRecord(
    @Id val id: UUID?,
    val userId: UUID,
    val title: String,
    val scheduleType: String,
    val scheduleTime: LocalTime,
    val scheduleDate: LocalDate?,
    val scheduleDayOfWeek: String?,
    val scheduleDayOfMonth: Short?,
    val enabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)