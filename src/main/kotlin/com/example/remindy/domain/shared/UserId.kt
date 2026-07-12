package com.example.remindy.domain.shared

import java.util.UUID

/**
 * ユーザーの識別子。User 集約の同一性であり、他集約(Reminder 等)からは
 * 「オブジェクト参照ではなく識別子で参照する」ため、この型だけを共有する。
 *
 * value class にしているのは、UUID を裸で回すと「どの ID か」を取り違える
 * (userId と reminderId を引数で逆に渡す等) 事故を型で防ぎつつ、実行時の
 * ラッピングコストをゼロにするため。UUID 自体が常に妥当なので検証は不要。
 */
@JvmInline
value class UserId(val value: UUID)