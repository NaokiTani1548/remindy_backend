package com.example.remindy.application.reminder.intake

internal object IntakeMessages {
    fun forDraft(draft: ReminderDraft): String {
        val missing = draft.missingElements()
        if (missing.isEmpty()) return "この内容で登録しますか？"
        return missing.joinToString(" ") {
            when (it) {
                MissingElement.TITLE -> "リマインダーのタイトルを教えてください。"
                MissingElement.SCHEDULE -> "いつ通知しますか？（例: 明日の9時 / 毎日7時30分）"
            }
        }
    }
}
