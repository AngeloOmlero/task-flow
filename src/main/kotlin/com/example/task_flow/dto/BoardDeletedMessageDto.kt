package com.example.task_flow.dto

data class BoardDeletedMessageDto(
    val type: String, // Should be "BOARD_DELETED"
    val deletedBoardId: Long
)
