package com.example.remindy.domain.user

class PasswordHash private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 255

        fun of(hashed: String): PasswordHash {
            require(hashed.isNotBlank()) { "パスワードハッシュは空にできません" }
            require(hashed.length <= MAX_LENGTH) { "パスワードハッシュが長すぎます" }
            return PasswordHash(hashed)
        }
    }

    override fun equals(other: Any?) = other is PasswordHash && other.value == value
    override fun hashCode() = value.hashCode()
    override fun toString() = "PasswordHash(****)"
}
