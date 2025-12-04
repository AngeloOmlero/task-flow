package com.example.task_flow.controller

import com.example.task_flow.dto.AddMemberDto
import com.example.task_flow.dto.BoardDto
import com.example.task_flow.dto.BoardDeletedMessageDto // Import the new DTO
import com.example.task_flow.dto.BoardUpdateMessageDto
import com.example.task_flow.dto.CreateBoardDto
import com.example.task_flow.dto.UpdateBoardDto
import com.example.task_flow.exception.ForbiddenException
import com.example.task_flow.service.BoardService
import com.example.task_flow.service.BroadcastService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val broadcast: BroadcastService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllBoards(principal: Principal?): List<BoardDto> {
        val username = principal?.name ?: throw ForbiddenException("Authentication required to view boards.")
        return boardService.getAllBoards(username)
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBoard(
        @Valid @RequestBody createBoardDto: CreateBoardDto,
        principal: Principal?
    ): BoardDto{
        val username = principal?.name ?: throw ForbiddenException("Authentication required to create a board.")
        val savedBoard = boardService.createBoard(createBoardDto, username)

        broadcast.sendBoardsCollectionUpdate(
            BoardUpdateMessageDto("BOARD_CREATED", savedBoard)
        )

        return savedBoard
    }

    @PutMapping("/{boardId}")
    @ResponseStatus(HttpStatus.OK)
    fun update(
        @PathVariable boardId: Long,
        @Valid @RequestBody updateBoardDto: UpdateBoardDto,
        principal: Principal?

    ): BoardDto{
        val username = principal?.name ?: throw ForbiddenException("Authentication required to update a board.")
        val updated = boardService.updateBoard(boardId, updateBoardDto, username)

        broadcast.sendBoardsUpdate(
            boardId,
            BoardUpdateMessageDto("BOARD_UPDATED",  updated)
        )

        return updated
    }

    @DeleteMapping("/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable boardId: Long,
        principal: Principal?
    ){
        val username = principal?.name ?: throw ForbiddenException("Authentication required to delete a board.")
        val board = boardService.getBoardById(boardId,username)

        boardService.deleteBoard(boardId, username)

        // Send BoardDeletedMessageDto for full board deletion
        broadcast.sendBoardsUpdate(
            boardId,
            BoardDeletedMessageDto("BOARD_DELETED", board.id!!)
        )
    }

    @PostMapping("/{boardId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    fun addMember(
        @PathVariable boardId: Long,
        @Valid @RequestBody addMemberDto: AddMemberDto,
        principal: Principal?
    ): BoardDto{
        val username = principal?.name ?: throw ForbiddenException("Authentication required to add a member to a board.")
        val updatedBoard = boardService.addMember(boardId,addMemberDto,username)

        // Notify the added member directly with a BOARD_CREATED message
        val addedMember = updatedBoard.members.find { it.id == addMemberDto.userId }
        addedMember?.username?.let {
            broadcast.sendToUserBoardsTopic(it, BoardUpdateMessageDto("BOARD_CREATED", updatedBoard))
        }

        // Notify all existing board members about the member addition
        broadcast.sendBoardsUpdate(
            boardId,
            BoardUpdateMessageDto("MEMBER_ADDED", updatedBoard)
        )

        return updatedBoard
    }

    @DeleteMapping("/{boardId}/members/{memberId}")
    fun removeMember(
        @PathVariable boardId: Long,
        @PathVariable memberId: Long,
        principal: Principal?
    ): BoardDto {
        val username = principal?.name ?: throw ForbiddenException("Authentication required.")
        val boardBeforeRemoval = boardService.getBoardById(boardId, username) // Get board details before removal
        val removedMemberUsername = boardBeforeRemoval.members.find { it.id == memberId }?.username

        val updatedBoard = boardService.removeMember(boardId, memberId, username)

        // Notify the removed member directly with a BOARD_DELETED message
        removedMemberUsername?.let {
            broadcast.sendToUserBoardsTopic(it, BoardDeletedMessageDto("BOARD_DELETED", boardBeforeRemoval.id!!)) // Send the board ID that was removed
        }

        // Notify all remaining board members about the member removal
        broadcast.sendBoardsUpdate(
            boardId,
            BoardUpdateMessageDto("MEMBER_REMOVED", updatedBoard)
        )

        return updatedBoard
    }

}
