package com.example.remindy.domain.reminder

import com.example.remindy.domain.shared.UserId

/**
 * リマインダーの永続化ポート(インターフェース)。実装は infrastructure 層。
 * ドメインは「何を保存/取得したいか」だけを宣言し、"どう保存するか"(SQL/JDBC)は知らない。
 *
 * save の契約: 未永続(id=null)なら採番して INSERT、既存(id有り)なら UPDATE し、
 * いずれも id が確定した Reminder を返す。新規/更新の判定方法(Spring Data JDBC の
 * null-id 検出)は実装側の関心事で、この宣言には現れない。
 */
interface ReminderRepository {
    fun save(reminder: Reminder): Reminder
    fun findById(id: ReminderId): Reminder?
    fun findByUserId(userId: UserId): List<Reminder>
    fun deleteById(id: ReminderId)
}