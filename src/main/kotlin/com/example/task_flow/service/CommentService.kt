package com.example.task_flow.service


import com.example.task_flow.dto.CommentDto
import com.example.task_flow.dto.CreateCommentDto
import com.example.task_flow.dto.UserDto
import com.example.task_flow.exception.ForbiddenException
import com.example.task_flow.exception.ResourceNotFoundException
import com.example.task_flow.model.Comment
import com.example.task_flow.repository.CommentRepository
import com.example.task_flow.repository.TaskRepository
import com.example.task_flow.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    fun createComment(taskId: Long, createCommentDto: CreateCommentDto, username: String): CommentDto {
        val task = taskRepository.findById(taskId).orElseThrow { ResourceNotFoundException("Task not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        val board = task.board
        if (board?.owner?.id != user.id && (board?.members?.none { it.id == user.id } != false)) {
            throw ForbiddenException("Only board members can comment on tasks.")
        }
        val comment = Comment()
        comment.content = createCommentDto.content
        comment.task = task
        comment.author = user
        val savedComment = commentRepository.save(comment)
        return toCommentDto(savedComment)
    }

    private fun toCommentDto(comment: Comment): CommentDto {
        val id = comment.id ?: throw IllegalStateException("Comment ID cannot be null.")
        val content = comment.content
        val author = comment.author ?: throw IllegalStateException("Comment author cannot be null.")
        val taskId = comment.task?.id ?: throw IllegalStateException("Comment task ID cannot be null.")
        val createdAt = comment.createdAt ?: throw IllegalStateException("Comment createdAt cannot be null.")
        val updatedAt = comment.updatedAt ?: throw IllegalStateException("Comment updatedAt cannot be null.")

        val authorDto = UserDto(
            author.id ?: throw IllegalStateException("Author ID cannot be null."),
            author.username ?: throw IllegalStateException("Author username cannot be null.")
        )

        return CommentDto(
            id = id,
            content = content,
            author = authorDto,
            taskId = taskId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
