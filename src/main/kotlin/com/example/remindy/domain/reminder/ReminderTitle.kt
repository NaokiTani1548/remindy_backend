package com.example.remindy.domain.reminder

/**
 * リマインダーのタイトル(値オブジェクト)。
 * 「常に正しい状態でのみ存在する」を守るため、コンストラクタを private にし、
 * 生成は of() 経由に限定する。ここで前後空白を除去し、不変条件(非空・100字以内)を検証する。
 * varchar(100) という DB 制約と同じ最大長をドメイン側でも表明している点に注目。
 */
class ReminderTitle private constructor(val value: String) {

    companion object {
        const val MAX_LENGTH = 100

        fun of(raw: String): ReminderTitle {
            val trimmed = raw.trim()
            require(trimmed.isNotEmpty()) { "リマインダーのタイトルは空にできません" }
            require(trimmed.length <= MAX_LENGTH) {
                "リマインダーのタイトルは${MAX_LENGTH}文字以内にしてください（現在: ${trimmed.length}文字）"
            }
            return ReminderTitle(trimmed)
        }
    }
    override fun equals(other: Any?): Boolean = other is ReminderTitle && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}