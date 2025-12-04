package com.example.task_flow.service

import com.example.task_flow.dto.CreateTaskDto
import com.example.task_flow.dto.TaskDto
import com.example.task_flow.dto.UpdateTaskDto
import com.example.task_flow.dto.UserDto
import com.example.task_flow.exception.ForbiddenException
import com.example.task_flow.exception.ResourceNotFoundException
import com.example.task_flow.model.Task
import com.example.task_flow.repository.BoardRepository
import com.example.task_flow.repository.TaskRepository
import com.example.task_flow.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    fun getTasksByBoardId(boardId: Long, username: String): List<TaskDto> {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id && board.members.none { it.id == user.id }) {
            throw ForbiddenException("You do not have permission to view tasks on this board.")
        }
        return taskRepository.findByBoardId(boardId).map { toTaskDto(it) }
    }

    fun getTaskById(boardId: Long, taskId: Long, username: String): TaskDto {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id && board.members.none { it.id == user.id }) {
            throw ForbiddenException("You do not have permission to view this task.")
        }
        val task = taskRepository.findById(taskId).orElseThrow { ResourceNotFoundException("Task not found") }
        if (task.board?.id != boardId) {
            throw ForbiddenException("This task does not belong to the specified board.")
        }
        return toTaskDto(task)
    }

    fun createTask(boardId: Long, createTaskDto: CreateTaskDto, username: String): TaskDto {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id && board.members.none { it.id == user.id }) {
            throw ForbiddenException("Only board members can create tasks.")
        }
        val task = Task()
        task.title = createTaskDto.title
        task.description = createTaskDto.description
        task.status = createTaskDto.status // New field
        task.priority = createTaskDto.priority // New field
        task.dueDate = createTaskDto.dueDate // New field
        task.board = board

        createTaskDto.assigneeIds?.let { ids ->
            task.assignees.addAll(userRepository.findAllById(ids))
        }

        val savedTask = taskRepository.save(task)
        return toTaskDto(savedTask)
    }

    fun updateTask(boardId: Long, taskId: Long, updateTaskDto: UpdateTaskDto, username: String): TaskDto {
        val task = taskRepository.findById(taskId).orElseThrow { ResourceNotFoundException("Task not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        if (board.owner?.id != user.id && board.members.none { it.id == user.id }) {
            throw ForbiddenException("Only board members can update tasks.")
        }

        updateTaskDto.title?.let { task.title = it }
        updateTaskDto.description?.let { task.description = it }
        updateTaskDto.status?.let { task.status = it } // New field
        updateTaskDto.priority?.let { task.priority = it } // New field
        updateTaskDto.dueDate?.let { task.dueDate = it } // New field

        updateTaskDto.assigneeIds?.let { ids ->
            task.assignees.clear() // Clear existing assignees
            task.assignees.addAll(userRepository.findAllById(ids)) // Add new assignees
        }

        val updatedTask = taskRepository.save(task)
        return toTaskDto(updatedTask)
    }

    fun deleteTask(boardId: Long, taskId: Long, username: String) {
        val task = taskRepository.findById(taskId).orElseThrow { ResourceNotFoundException("Task not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        if (board.owner?.id != user.id && board.members.none { it.id == user.id }) {
            throw ForbiddenException("Only board members can delete tasks.")
        }
        taskRepository.delete(task)
    }

    private fun toTaskDto(task: Task): TaskDto {
        val id = task.id ?: throw IllegalStateException("Task ID cannot be null.")
        val title = task.title
        val status = task.status
        val priority = task.priority
        val boardId = task.board?.id ?: throw IllegalStateException("Task board ID cannot be null.")
        val createdAt = task.createdAt ?: throw IllegalStateException("Task createdAt cannot be null.")
        val updatedAt = task.updatedAt ?: throw IllegalStateException("Task updatedAt cannot be null.")

        val assigneesDto = task.assignees.map { assignee ->
            UserDto(
                assignee.id ?: throw IllegalStateException("Assignee ID cannot be null."),
                assignee.username ?: throw IllegalStateException("Assignee username cannot be null.")
            )
        }.toSet()

        return TaskDto(
            id = id,
            title = title,
            description = task.description,
            status = status,
            priority = priority,
            dueDate = task.dueDate,
            assignees = assigneesDto,
            boardId = boardId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
