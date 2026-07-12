package com.example.remindy.presentation.rest.study

import com.example.remindy.domain.study.StudyItem
import com.example.remindy.domain.study.StudyItemKind
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateStudyItemRequest(
    @field:NotNull val kind: StudyItemKind,
    @field:NotBlank val prompt: String,
    @field:NotBlank val answer: String,
)

data class UpdateStudyItemRequest(
    @field:NotNull val kind: StudyItemKind,
    @field:NotBlank val prompt: String,
    @field:NotBlank val answer: String,
)

data class ToggleStudyItemRequest(val enabled: Boolean)

data class StudyItemResponse(
    val id: String,
    val kind: StudyItemKind,
    val prompt: String,
    val answer: String,
    val enabled: Boolean,
) {
    companion object {
        fun from(item: StudyItem) = StudyItemResponse(
            id = item.id!!.value.toString(),
            kind = item.kind,
            prompt = item.prompt.value,
            answer = item.answer.value,
            enabled = item.enabled,
        )
    }
}

data class StudyItemListResponse(val items: List<StudyItemResponse>)
