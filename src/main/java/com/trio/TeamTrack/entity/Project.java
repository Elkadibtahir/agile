package com.trio.TeamTrack.entity;

import com.trio.TeamTrack.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_owner", columnList = "owner_id")
})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(unique = true)
    private String invitationCode;

    private LocalDateTime invitationCodeCreatedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Plusieurs projets → un seul owner
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Un projet → plusieurs tâches
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // Un projet → plusieurs membres
    @Setter
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Calcule le % de progression (tâches DONE / total)
    public int getProgressPercentage() {
        if (tasks == null || tasks.isEmpty()) return 0;
        int done = 0;
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.DONE) done++;
        }
        return (done * 100) / tasks.size();
    }

    // Le projet est-il en retard ?
    public boolean isOverdue() {
        return endDate != null
            && LocalDate.now().isAfter(endDate)
            && getProgressPercentage() < 100;
    }

    // Compte les tâches par statut
    public int countByStatus(TaskStatus status) {
        int count = 0;
        for (Task t : tasks) {
            if (t.getStatus() == status) count++;
        }
        return count;
    }

    // ---- Getters et Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getInvitationCode() { return invitationCode; }
    public void setInvitationCode(String invitationCode) { this.invitationCode = invitationCode; }

    public LocalDateTime getInvitationCodeCreatedAt() { return invitationCodeCreatedAt; }
    public void setInvitationCodeCreatedAt(LocalDateTime invitationCodeCreatedAt) { this.invitationCodeCreatedAt = invitationCodeCreatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<ProjectMember> getMembers() { return members; }


}
