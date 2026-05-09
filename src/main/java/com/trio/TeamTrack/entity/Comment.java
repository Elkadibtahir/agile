package com.trio.TeamTrack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_task", columnList = "task_id"),
    @Index(name = "idx_comment_task_date", columnList = "task_id, createdAt DESC")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Sentiment analysé par AiService : POSITIVE, NEUTRAL, NEGATIVE
    private String sentiment = "NEUTRAL";

    // Plusieurs commentaires → une seule tâche
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // Plusieurs commentaires → un seul auteur
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---- Getters et Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}
