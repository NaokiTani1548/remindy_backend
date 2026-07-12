package com.example.remindy.infrastructure.persistence.reminder

import org.springframework.data.repository.CrudRepository
import java.util.UUID

/**
 * SpringDataJDBC が実装を自動生成する低レベルCRUD。infra 内部でのみ使う。
 * ドメインの ReminderRepository ポートとは別物(こちらは技術的詳細)。
 */
interface ReminderJdbcRepository : CrudRepository<ReminderRecord, UUID> {
    fun findByUserId(userId: UUID): List<ReminderRecord>
}