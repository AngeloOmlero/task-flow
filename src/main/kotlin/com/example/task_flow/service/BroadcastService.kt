package com.example.task_flow.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class BroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun sendBoardsUpdate(boardId: Long, message: Any) {
        messagingTemplate.convertAndSend("/topic/boards/$boardId", message)
    }

    fun sendBoardsCollectionUpdate(message: Any) {
        messagingTemplate.convertAndSend("/topic/boards", message)
    }

    fun sendTaskUpdate(boardId: Long, taskId: Long, message: Any) {
        messagingTemplate.convertAndSend("/topic/boards/$boardId/tasks", message)
        messagingTemplate.convertAndSend("/topic/tasks/$taskId", message)
    }

    fun sendCommentUpdate(taskId: Long, message: Any) {
        messagingTemplate.convertAndSend("/topic/tasks/$taskId/comments", message)
    }

    fun sendToUserBoardsTopic(username: String, message: Any) {
        messagingTemplate.convertAndSendToUser(username, "/topic/boards", message)
    }
}
