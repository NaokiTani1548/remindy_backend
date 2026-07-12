package com.example.remindy.infrastructure.persistence.reminder

import com.example.remindy.domain.reminder.*
import com.example.remindy.domain.shared.UserId
import org.springframework.stereotype.Repository
import java.time.Clock

/**
 * ドメインの ReminderRepository ポートを infra で実装。
 * ・@Repository で Spring 管理。application 層はこの実装を知らずポートに依存する。
 * ・Clock を注入し、保存時の監査時刻に使う。
 * ・SpringData の JdbcRepository と Mapper を束ねてドメイン語彙に翻訳する。
 */
@Repository
class ReminderRepositoryImpl(
    private val jdbc: ReminderJdbcRepository,
    private val clock: Clock,
) : ReminderRepository {

    override fun save(reminder: Reminder): Reminder {
        val saved = jdbc.save(ReminderMapper.toRecord(reminder, clock))
        return ReminderMapper.toDomain(saved)
    }

    override fun findById(id: ReminderId): Reminder? =
        jdbc.findById(id.value).map(ReminderMapper::toDomain).orElse(null)

    override fun findByUserId(userId: UserId): List<Reminder> =
        jdbc.findByUserId(userId.value).map(ReminderMapper::toDomain)

    override fun deleteById(id: ReminderId) = jdbc.deleteById(id.value)
}