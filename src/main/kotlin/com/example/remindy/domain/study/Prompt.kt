package com.example.remindy.domain.study

class Prompt private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 500
        fun of(raw: String): Prompt {
            val trimmed = raw.trim()
            require(trimmed.isNotEmpty()) { "表(prompt)は空にできません" }
            require(trimmed.length <= MAX_LENGTH) { "表(prompt)は${MAX_LENGTH}文字以内にしてください" }
            return Prompt(trimmed)
        }
    }
    override fun equals(other: Any?) = other is Prompt && other.value == value
    override fun hashCode() = value.hashCode()
    override fun toString() = value
}
