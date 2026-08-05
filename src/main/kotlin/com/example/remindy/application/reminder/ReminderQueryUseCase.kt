package com.example.remindy.application.reminder

import com.example.remindy.domain.reminder.Reminder
import com.example.remindy.domain.reminder.ReminderId
import com.example.remindy.domain.reminder.ReminderNotFoundException
import com.example.remindy.domain.reminder.ReminderRepository
import com.example.remindy.domain.shared.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 参照系ユースケース(Query側)。CQRSの作法によりコマンドオブジェクトは用いず引数直渡し。
 * 認可(所有者確認)はこの層の責務(要件FR-A4)。
 */
@Service
@Transactional(readOnly = true)
class ReminderQueryUseCase(
    private val reminderRepository: ReminderRepository,
) {
    /** 一覧: DBクエリ段階で userId に絞るため他人のものは混入しない。 */
    fun list(userId: UserId): List<Reminder> =
        reminderRepository.findByUserId(userId)

    /** 単一取得: 存在しない、または他人のものは一律 NotFound(=404で存在秘匿。API仕様Q5)。 */
    fun get(userId: UserId, id: ReminderId): Reminder =
        reminderRepository.findById(id)
            ?.takeIf { it.userId == userId && !it.isDeleted }
            ?: throw ReminderNotFoundException(id)
}