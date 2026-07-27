package com.example.remindy.presentation.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import javax.sql.DataSource

@RestController
@RequestMapping("/health")
class HealthController(
    private val dataSource: DataSource,
) {
    @GetMapping
    fun health(): ResponseEntity<Void> =
        if (isDatabaseReady()) ResponseEntity.ok().build()
        else ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()

    @GetMapping("/connect")
    fun connect(): DeferredResult<ResponseEntity<Void>> {
        val result = DeferredResult<ResponseEntity<Void>>(300_000L, ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build<Void>())

        if (isDatabaseReady()) {
            result.setResult(ResponseEntity.ok().build())
            return result
        }

        Thread.ofVirtual().start {
            while (!result.isSetOrExpired) {
                Thread.sleep(1_000)
                if (isDatabaseReady()) {
                    result.setResult(ResponseEntity.ok().build())
                    break
                }
            }
        }

        return result
    }

    private fun isDatabaseReady(): Boolean = try {
        dataSource.connection.use { it.isValid(1) }
    } catch (_: Exception) {
        false
    }
}
