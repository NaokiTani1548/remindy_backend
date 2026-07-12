package com.example.remindy.application.reminder

import com.example.remindy.application.reminder.command.CreateReminderCommand
import com.example.remindy.domain.reminder.Reminder
import com.example.remindy.domain.reminder.ReminderRepository
import com.example.remindy.domain.reminder.ReminderTitle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * リマインダー作成ユースケース。
 * ・@Service で Spring 管理下に(ここから外側の層は Spring 依存でよい)。
 * ・@Transactional で「検証→生成→保存」を1トランザクションに。
 * ・依存はドメインのポート(ReminderRepository)のみ。実装(infra)は知らない=依存方向を内向きに保つ。
 */
@Service
class CreateReminderUseCase(
    private val reminderRepository: ReminderRepository,
) {
    @Transactional
    fun handle(command: CreateReminderCommand): Reminder {
        val title = ReminderTitle.of(command.title)
        val reminder = Reminder.create(
            userId = command.userId,
            title = title,
            schedule = command.schedule,
        )
        return reminderRepository.save(reminder)
    }
}