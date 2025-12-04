package com.example.task_flow.repository

import com.example.task_flow.model.Task
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByBoardId(boardId: Long): List<Task>
}
