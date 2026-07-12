package com.example.remindy.domain.study

class StudyItemNotFoundException(id: StudyItemId) :
    RuntimeException("StudyItem not found: ${id.value}")
