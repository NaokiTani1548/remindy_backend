package com.example.remindy.domain.study

class Answer private constructor(val value: String) {
    companion object {
        fun of(raw: String): Answer {
            val trimmed = raw.trim()
            require(trimmed.isNotEmpty()) { "裏(answer)は空にできません" }
            return Answer(trimmed)
        }
    }
    override fun equals(other: Any?) = other is Answer && other.value == value
    override fun hashCode() = value.hashCode()
    override fun toString() = value
}
