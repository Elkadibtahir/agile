package com.trio.TeamTrack.repository;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.Task;
import com.trio.TeamTrack.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Liste simple (pour le Kanban)
    List<Task> findByProject(Project project);

    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    // Version paginée (pour la liste des tâches)
    Page<Task> findByProject(Project project, Pageable pageable);

    long countByProjectAndStatus(Project project, TaskStatus status);

    // Vélocité : tâches terminées depuis une date donnée
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project " +
            "AND t.status = 'DONE' AND t.updatedAt >= :since")
    long countCompletedSince(@Param("project") Project project,
                             @Param("since") LocalDateTime since);
}
