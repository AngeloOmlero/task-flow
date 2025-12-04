package com.example.task_flow.controller


import com.example.task_flow.dto.CreateTaskDto
import com.example.task_flow.dto.TaskDto
import com.example.task_flow.dto.TaskUpdateMessageDto
import com.example.task_flow.dto.UpdateTaskDto
import com.example.task_flow.exception.ForbiddenException
import com.example.task_flow.service.BroadcastService
import com.example.task_flow.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/boards/{boardId}/tasks")
class TaskController(
    private val taskService: TaskService,
    private val broadcast: BroadcastService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getTasksByBoardId(@PathVariable boardId: Long, principal: Principal?): List<TaskDto> {
        val username = principal?.name ?: throw ForbiddenException("Authentication required to view tasks.")
        return taskService.getTasksByBoardId(boardId, username)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(
        @PathVariable boardId: Long,
        @Valid @RequestBody createTaskDto: CreateTaskDto,
        principal: Principal?
    ): TaskDto {
        val username = principal?.name ?: throw ForbiddenException("Authentication required")
        val saved= taskService.createTask(boardId, createTaskDto, username)

        broadcast.sendTaskUpdate(
            boardId,
            saved.id,
            TaskUpdateMessageDto("TASK_CREATED", saved)
        )

        return saved
    }

    @PutMapping("/{taskId}")
    fun updateTask(
        @PathVariable boardId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody dto: UpdateTaskDto,
        principal: Principal?
    ): TaskDto {
        val username = principal?.name ?: throw ForbiddenException("Authentication required.")
        val updated = taskService.updateTask(boardId, taskId, dto, username)

        broadcast.sendTaskUpdate(
            boardId, taskId,
            TaskUpdateMessageDto("TASK_UPDATED", updated)
        )

        return updated
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(
        @PathVariable boardId: Long,
        @PathVariable taskId: Long,
        principal: Principal?
    ) {
        val username = principal?.name ?: throw ForbiddenException("Authentication required.")
        val deleted = taskService.getTaskById(boardId, taskId, username)

        taskService.deleteTask(boardId, taskId, username)

        broadcast.sendTaskUpdate(
            boardId, taskId,
            TaskUpdateMessageDto("TASK_DELETED", deleted)
        )
    }

}
