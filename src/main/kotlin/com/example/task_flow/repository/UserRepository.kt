package com.example.task_flow.repository

import com.example.task_flow.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<User,Long> {
    fun findByUsername(username:String): User?
}