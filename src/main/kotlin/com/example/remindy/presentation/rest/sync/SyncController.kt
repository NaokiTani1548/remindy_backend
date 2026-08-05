package com.example.remindy.presentation.rest.sync

import com.example.remindy.application.sync.GetSyncSnapshotUseCase
import com.example.remindy.application.sync.SyncUseCase
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sync")
class SyncController(
    private val getSnapshot: GetSyncSnapshotUseCase,
    private val syncUseCase: SyncUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @GetMapping
    fun snapshot(): SyncResponse =
        SyncResponse.from(getSnapshot.handle(currentUser.currentUserId()))

    @PostMapping
    fun sync(@Valid @RequestBody request: SyncPushRequest): SyncPushResponse {
        val result = syncUseCase.handle(currentUser.currentUserId(), request.toCommand())
        return SyncPushResponse.from(result)
    }
}
