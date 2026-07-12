package com.example.remindy.presentation.rest.sync

import com.example.remindy.application.sync.GetSyncSnapshotUseCase
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sync")
class SyncController(
    private val getSnapshot: GetSyncSnapshotUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @GetMapping
    fun sync(): SyncResponse =
        SyncResponse.from(getSnapshot.handle(currentUser.currentUserId()))
}
