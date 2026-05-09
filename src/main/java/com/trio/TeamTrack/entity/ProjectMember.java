package com.trio.TeamTrack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String role = "Membre";

    @Column(updatable = false)
    private LocalDateTime joinedAt;

    // Plusieurs membres → un seul projet
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Plusieurs membres → un seul utilisateur
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void onJoin() {
        this.joinedAt = LocalDateTime.now();
    }

    // ---- Getters et Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
