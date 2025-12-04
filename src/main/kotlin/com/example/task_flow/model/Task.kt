package com.example.task_flow.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "tasks")
class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    var status: String = "TODO", // New field: status

    @Enumerated(EnumType.STRING) // New field: priority
    @Column(nullable = false)
    var priority: TaskPriority = TaskPriority.LOW,

    @Column(name = "due_date") // New field: dueDate
    var dueDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnore
    var board: Board? = null,

    @ManyToMany(fetch = FetchType.LAZY) // Changed from ManyToOne assignee to ManyToMany assignees
    @JoinTable(
        name = "task_assignees",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var assignees: MutableSet<User> = mutableSetOf(),

    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var comments: MutableList<Comment> = mutableListOf(),

    @CreationTimestamp // Added creation timestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp // Added update timestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {
    // Default constructor for JPA
    constructor(): this(null, "", null, "TODO", TaskPriority.LOW, null, null, mutableSetOf(), mutableListOf(), null, null)
}
