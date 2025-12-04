package com.example.task_flow.dto

data class CommentUpdateMessageDto(
    val type: String, // COMMENT_CREATED, COMMENT_UPDATED, COMMENT_DELETED
    val comment: CommentDto
)

