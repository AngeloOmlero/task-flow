package com.example.task_flow.service


import com.example.task_flow.dto.AddMemberDto
import com.example.task_flow.dto.BoardDto
import com.example.task_flow.dto.CreateBoardDto
import com.example.task_flow.dto.UpdateBoardDto
import com.example.task_flow.dto.UserDto
import com.example.task_flow.exception.ForbiddenException
import com.example.task_flow.exception.ResourceNotFoundException
import com.example.task_flow.model.Board
import com.example.task_flow.repository.BoardRepository
import com.example.task_flow.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    fun createBoard(createBoardDto: CreateBoardDto, username: String): BoardDto {
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        val board = Board()
        board.title = createBoardDto.title
        board.description = createBoardDto.description
        board.owner = user
        board.members.add(user) // Add the owner as a member
        val savedBoard = boardRepository.save(board)
        return toBoardDto(savedBoard)
    }

    fun updateBoard(boardId: Long, updateBoardDto: UpdateBoardDto, username: String): BoardDto {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id) {
            throw ForbiddenException("Only the board owner can update the board.")
        }
        board.title = updateBoardDto.title
        board.description = updateBoardDto.description
        val updatedBoard = boardRepository.save(board)
        return toBoardDto(updatedBoard)
    }

    fun deleteBoard(boardId: Long, username: String): Map<String, Long> {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id) {
            throw ForbiddenException("Only the board owner can delete the board.")
        }
        val boardIdLong = board.id ?: throw IllegalStateException("Board ID cannot be null for deletion.")
        boardRepository.delete(board)
        return mapOf("deletedBoardId" to boardIdLong)
    }

    fun addMember(boardId: Long, addMemberDto: AddMemberDto, username: String): BoardDto {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id) {
            throw ForbiddenException("Only the board owner can add members.")
        }
        val member = userRepository.findById(addMemberDto.userId).orElseThrow { ResourceNotFoundException("User not found") }
        board.members.add(member)
        val updatedBoard = boardRepository.save(board)
        return toBoardDto(updatedBoard)
    }

    fun removeMember(boardId: Long, memberId: Long, username: String): BoardDto {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id) {
            throw ForbiddenException("Only the board owner can remove members.")
        }
        if (board.owner?.id == memberId) {
            throw ForbiddenException("The board owner cannot be removed.")
        }
        val memberToRemove = userRepository.findById(memberId).orElseThrow { ResourceNotFoundException("Member not found") }
        board.members.remove(memberToRemove)
        val updatedBoard = boardRepository.save(board)
        return toBoardDto(updatedBoard)
    }

    fun getAllBoards(username: String): List<BoardDto> {
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        return boardRepository.findByOwnerUsernameOrMembersUsername(user.username!!).map { toBoardDto(it) }
    }

    fun getBoardById(boardId: Long, username: String): BoardDto {
        val board = boardRepository.findById(boardId).orElseThrow { ResourceNotFoundException("Board not found") }
        val user = userRepository.findByUsername(username) ?: throw ResourceNotFoundException("User not found")
        if (board.owner?.id != user.id && board.members.none { it.id == user.id }) {
            throw ForbiddenException("You do not have permission to view this board.")
        }
        return toBoardDto(board)
    }

    private fun toBoardDto(board: Board): BoardDto {
        val id = board.id ?: throw IllegalStateException("Board ID cannot be null.")
        val title = board.title
        val description = board.description
        val owner = board.owner ?: throw IllegalStateException("Board owner cannot be null for Board ID: ${board.id}")
        val createdAt = board.createdAt ?: throw IllegalStateException("Board createdAt cannot be null for Board ID: ${board.id}")
        val updatedAt = board.updatedAt ?: throw IllegalStateException("Board updatedAt cannot be null for Board ID: ${board.id}")

        val ownerDto = UserDto(
            owner.id ?: throw IllegalStateException("Owner ID cannot be null."),
            owner.username ?: throw IllegalStateException("Owner username cannot be null.")
        )

        val memberDtos = board.members.map { member ->
            UserDto(member.id ?: throw IllegalStateException("Member ID cannot be null."),
                    member.username ?: throw IllegalStateException("Member username cannot be null."))
        }.toSet()

        return BoardDto(id, title, description, ownerDto, memberDtos, createdAt, updatedAt)
    }
}
