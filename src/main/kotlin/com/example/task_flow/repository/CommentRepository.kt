package com.example.task_flow.repository

import com.example.task_flow.model.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByTaskId(taskId: Long): List<Comment>
}