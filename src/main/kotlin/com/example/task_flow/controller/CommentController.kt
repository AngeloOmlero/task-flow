package com.example.task_flow.controller


import com.example.task_flow.dto.CommentDto
import com.example.task_flow.dto.CommentUpdateMessageDto
import com.example.task_flow.dto.CreateCommentDto
import com.example.task_flow.exception.ForbiddenException
import com.example.task_flow.service.CommentService
import com.example.task_management.service.BroadcastService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
class CommentController(
    private val commentService: CommentService,
    private val broadcast: BroadcastService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(
        @PathVariable taskId: Long,
        @Valid @RequestBody dto: CreateCommentDto,
        principal: Principal?
    ): CommentDto {
        val username = principal?.name ?: throw ForbiddenException("Authentication required.")
        val saved = commentService.createComment(taskId, dto, username)

        broadcast.sendCommentUpdate(
            taskId,
            CommentUpdateMessageDto("COMMENT_CREATED", saved)
        )

        return saved
    }
}
