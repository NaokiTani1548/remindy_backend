package com.example.remindy.domain.user

class EmailAddress private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 254
        private val PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun of(raw: String): EmailAddress {
            val normalized = raw.trim().lowercase()
            require(normalized.isNotEmpty()) { "メールアドレスは空にできません" }
            require(normalized.length <= MAX_LENGTH) { "メールアドレスは${MAX_LENGTH}文字以内にしてください" }
            require(PATTERN.matches(normalized)) { "メールアドレスの形式が正しくありません" }
            return EmailAddress(normalized)
        }
    }

    override fun equals(other: Any?) = other is EmailAddress && other.value == value
    override fun hashCode() = value.hashCode()
    override fun toString() = value
}
