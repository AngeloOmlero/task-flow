package com.example.task_flow.dto

data class BoardUpdateMessageDto(
    val type: String, // BOARD_UPDATED, BOARD_DELETED, MEMBER_ADDED, MEMBER_REMOVED
    val board: BoardDto
)


