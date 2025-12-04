package com.example.task_flow.config


import com.example.task_flow.repository.BoardRepository
import com.example.task_flow.repository.TaskRepository
import com.example.task_flow.security.JWTUtil
import com.example.task_flow.security.UserDetailsServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class WebSocketAuthChannelInterceptor(
    private val jwtUtil: JWTUtil,
    private val userDetailsService: UserDetailsServiceImpl,
    private val boardRepository: BoardRepository,
    private val taskRepository: TaskRepository
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor::class.java)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message

        try { // Added try-catch block to log all exceptions
            when (accessor.command) {
                StompCommand.CONNECT -> {
                    val authorizationHeader = accessor.getFirstNativeHeader("Authorization")
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        val jwt = authorizationHeader.substring(7)
                        val username = jwtUtil.extractUsername(jwt)
                        if (username != null && jwtUtil.validateToken(jwt)) {
                            val userDetails = userDetailsService.loadUserByUsername(username)
                            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                            accessor.user = authentication
                            logger.info("WebSocket CONNECT: User '{}' authenticated successfully.", username)
                        } else {
                            logger.warn("WebSocket CONNECT: Invalid or expired JWT token for user '{}'.", username)
                            throw AccessDeniedException("Invalid or expired JWT token.")
                        }
                    } else {
                        logger.warn("WebSocket CONNECT: No Authorization header or invalid format.")
                        throw AccessDeniedException("Authentication required.")
                    }
                }
                StompCommand.SUBSCRIBE -> {
                    val user = accessor.user as? Principal
                    if (user == null) {
                        logger.warn("WebSocket SUBSCRIBE: Unauthenticated user attempted to subscribe to destination '{}'.", accessor.destination)
                        throw AccessDeniedException("User not authenticated.")
                    }
                    val destination = accessor.destination
                    if (destination == null) {
                        logger.warn("WebSocket SUBSCRIBE: User '{}' attempted to subscribe to a null destination.", user.name)
                        return message // Or throw AccessDeniedException if null destinations are strictly forbidden
                    }

                    logger.info("WebSocket SUBSCRIBE: User '{}' attempting to subscribe to destination '{}'.", user.name, destination)

                    val boardIdFromBoardTopic = extractBoardIdFromBoardTopic(destination)
                    if (boardIdFromBoardTopic != null) {
                        authorizeBoardAccess(boardIdFromBoardTopic, user.name)
                        logger.info("WebSocket SUBSCRIBE: User '{}' authorized for board topic '{}'.", user.name, destination)
                        return message
                    }

                    val taskIdFromTaskTopic = extractTaskIdFromTaskTopic(destination)
                    if (taskIdFromTaskTopic != null) {
                        val task = taskRepository.findById(taskIdFromTaskTopic).orElse(null)
                        if (task == null) {
                            logger.warn("WebSocket SUBSCRIBE: User '{}' attempted to subscribe to topic for non-existent task '{}'.", user.name, taskIdFromTaskTopic)
                            throw AccessDeniedException("Cannot subscribe to topic for a non-existent task.")
                        }
                        val boardId = task.board?.id
                        if (boardId != null) {
                            authorizeBoardAccess(boardId, user.name)
                            logger.info("WebSocket SUBSCRIBE: User '{}' authorized for task topic '{}'.", user.name, destination)
                        } else {
                            logger.warn("WebSocket SUBSCRIBE: User '{}' attempted to subscribe to task '{}' with null board ID.", user.name, taskIdFromTaskTopic)
                            throw AccessDeniedException("Task is not associated with a board.")
                        }
                        return message
                    }
                    
                    // Handle other topics if necessary, e.g., /user/topic/boards
                    if (destination.startsWith("/user/topic/boards")) {
                        logger.info("WebSocket SUBSCRIBE: User '{}' authorized for user-specific board topic '{}'.", user.name, destination)
                        return message
                    }

                    logger.warn("WebSocket SUBSCRIBE: User '{}' attempted to subscribe to an unauthorized destination '{}'.", user.name, destination)
                    throw AccessDeniedException("User does not have access to this topic.")
                }
                else -> {
                    // No-op for other STOMP commands like SEND, DISCONNECT
                    logger.debug("WebSocket {}: User '{}' to destination '{}'.", accessor.command, accessor.user?.name, accessor.destination)
                }
            }
        } catch (e: AccessDeniedException) {
            logger.warn("WebSocket Access Denied: {} - {}", e.message, accessor.destination)
            throw e // Re-throw to propagate the error
        } catch (e: Exception) {
            logger.error("WebSocket Interceptor unexpected error: {} - {}", e.message, accessor.destination, e)
            throw AccessDeniedException("Internal server error during WebSocket authorization.") // Generic error for client
        }
        return message
    }

    private fun authorizeBoardAccess(boardId: Long, username: String) {
        val board = boardRepository.findByIdWithDetails(boardId) // Use findByIdWithDetails
            ?: run {
                logger.warn("Authorization failed: Board '{}' not found.", boardId)
                throw AccessDeniedException("Cannot subscribe to topic for a non-existent board.")
            }

        val isOwner = board.owner?.username == username
        val isMember = board.members.any { it.username == username }

        logger.info("Authorization check for board '{}': User '{}' isOwner={}, isMember={}", boardId, username, isOwner, isMember)

        if (!isOwner && !isMember) {
            logger.warn("Authorization failed: User '{}' is neither owner nor member of board '{}'.", username, boardId)
            throw AccessDeniedException("User does not have access to this board's topic.")
        }
    }

    private fun extractBoardIdFromBoardTopic(destination: String): Long? {
        val regex = Regex("/topic/boards/(\\d+)")
        return regex.find(destination)?.groups?.get(1)?.value?.toLongOrNull()
    }

    private fun extractTaskIdFromTaskTopic(destination: String): Long? {
        val regex = Regex("/topic/tasks/(\\d+)")
        return regex.find(destination)?.groups?.get(1)?.value?.toLongOrNull()
    }
}
