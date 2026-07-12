package com.example.remindy.presentation.rest.study

import com.example.remindy.application.reminder.command.SetReminderEnabledCommand
import com.example.remindy.application.study.*
import com.example.remindy.application.study.command.CreateStudyItemCommand
import com.example.remindy.application.study.command.DeleteStudyItemCommand
import com.example.remindy.application.study.command.SetStudyItemEnabledCommand
import com.example.remindy.application.study.command.UpdateStudyItemCommand
import com.example.remindy.domain.study.StudyItemId
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/v1/study-items")
class StudyItemController(
    private val create: CreateStudyItemUseCase,
    private val query: StudyItemQueryUseCase,
    private val update: UpdateStudyItemUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody req: CreateStudyItemRequest,
        uriBuilder: UriComponentsBuilder,
    ): ResponseEntity<StudyItemResponse> {
        val item = create.handle(
            CreateStudyItemCommand(
                currentUser.currentUserId(), req.kind, req.prompt, req.answer
            )
        )
        val location = uriBuilder.path("/api/v1/study-items/{id}")
            .buildAndExpand(item.id!!.value).toUri()
        return ResponseEntity.created(location).body(StudyItemResponse.from(item))
    }

    @GetMapping
    fun list(): StudyItemListResponse =
        StudyItemListResponse(query.list(currentUser.currentUserId()).map(StudyItemResponse::from))

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): StudyItemResponse =
        StudyItemResponse.from(query.get(currentUser.currentUserId(), StudyItemId(id)))

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: UpdateStudyItemRequest): StudyItemResponse =
        StudyItemResponse.from(
            update.changeContent(
                UpdateStudyItemCommand(
                    currentUser.currentUserId(), StudyItemId(id), req.kind, req.prompt, req.answer
                )
            )
        )

    @PatchMapping("/{id}")
    fun toggle(@PathVariable id: UUID, @RequestBody req: ToggleStudyItemRequest): StudyItemResponse =
        StudyItemResponse.from(update.setEnabled(
            SetStudyItemEnabledCommand(
                currentUser.currentUserId(), StudyItemId(id), req.enabled)
            )
        )

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        update.delete(
            DeleteStudyItemCommand(
                currentUser.currentUserId(), StudyItemId(id)
            )
        )
        return ResponseEntity.noContent().build()
    }
}
