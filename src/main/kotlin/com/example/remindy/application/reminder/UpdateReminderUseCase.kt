package com.example.remindy.application.reminder

import com.example.remindy.application.reminder.command.DeleteReminderCommand
import com.example.remindy.application.reminder.command.SetReminderEnabledCommand
import com.example.remindy.application.reminder.command.UpdateReminderCommand
import com.example.remindy.domain.reminder.*
import com.example.remindy.domain.shared.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * リマインダーの変更系ユースケース(Command側)。入力はすべてコマンドオブジェクトで受ける。
 * 認可は load() に集約し、全変更系がこれを通ることで認可漏れを構造的に防ぐ。
 */
@Service
class UpdateReminderUseCase(
    private val reminderRepository: ReminderRepository,
) {
    /** PUT /reminders/{id}: 内容(タイトル・スケジュール)を置換。 */
    @Transactional
    fun changeContent(command: UpdateReminderCommand): Reminder {
        val current = load(command.userId, command.id)          // 取得＋認可
        val updated = current.changeContent(
            ReminderTitle.of(command.title),                    // 不変条件の検証はドメインVOに委ねる
            command.schedule,
        )
        return reminderRepository.save(updated)                 // id有り→UPDATE、createdAtは実装側で維持
    }

    /** PATCH /reminders/{id}: 有効/無効の切替。冪等。 */
    @Transactional
    fun setEnabled(command: SetReminderEnabledCommand): Reminder {
        val current = load(command.userId, command.id)
        val updated = if (command.enabled) current.enable() else current.disable()
        return reminderRepository.save(updated)
    }

    /** DELETE /reminders/{id}: 存在＋所有者を確認してから削除。 */
    @Transactional
    fun delete(command: DeleteReminderCommand) {
        load(command.userId, command.id)
        reminderRepository.deleteById(command.id)
    }

    /**
     * 取得＋所有者チェック。他人の資源は「無かったこと」にして存在を秘匿する(API仕様Q5)。
     * 403 を返すと存在が漏れるため、NotFound に合流させるのが定石。
     */
    private fun load(userId: UserId, id: ReminderId): Reminder =
        reminderRepository.findById(id)
            ?.takeIf { it.userId == userId && !it.isDeleted }
            ?: throw ReminderNotFoundException(id)
}