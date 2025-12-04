package com.example.task_flow.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "boards")
class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String = "", // Changed from 'name' to 'title'

    @Column(columnDefinition = "TEXT") // Added description field
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    var owner: User? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "board_members",
        joinColumns = [JoinColumn(name = "board_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var members: MutableSet<User> = mutableSetOf(),

    @OneToMany(mappedBy = "board", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    var tasks: MutableList<Task> = mutableListOf(),

    @CreationTimestamp // Added creation timestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp // Added update timestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {
    // Default constructor for JPA
    constructor(): this(null, "", null, null, mutableSetOf(), mutableListOf(), null, null)
}
