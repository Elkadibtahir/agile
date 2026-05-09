package com.trio.TeamTrack.repository;

import com.trio.TeamTrack.entity.Comment;
import com.trio.TeamTrack.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Récupère les commentaires du plus récent au plus ancien
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);
}
