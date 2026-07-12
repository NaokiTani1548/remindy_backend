package com.example.remindy.presentation.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 例外→RFC9457 ProblemDetail 変換を一元化(API仕様2.3/Q4)。
 * ドメインの require 由来 IllegalArgumentException を 400 に写す。
 */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIllegalArgument(e: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "不正な入力です").apply {
            title = "Validation failed"
        }
}
