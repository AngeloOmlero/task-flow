package com.example.task_flow.repository


import com.example.task_flow.model.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BoardRepository : JpaRepository<Board, Long> {
    @Query("SELECT DISTINCT b FROM Board b LEFT JOIN FETCH b.owner LEFT JOIN FETCH b.members m WHERE b.owner.username = :username OR m.username = :username")
    fun findByOwnerUsernameOrMembersUsername(@Param("username") username: String): List<Board>

    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.owner LEFT JOIN FETCH b.members WHERE b.id = :boardId")
    fun findByIdWithDetails(@Param("boardId") boardId: Long): Board?
}
