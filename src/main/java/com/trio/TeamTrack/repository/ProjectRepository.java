package com.trio.TeamTrack.repository;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByInvitationCode(String invitationCode);

    // Tous les projets (owner OU membre) — liste simple
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
            "WHERE p.owner = :user OR m.user = :user ORDER BY p.createdAt DESC")
    List<Project> findAllForUser(@Param("user") User user);

    // Version paginée
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
            "WHERE p.owner = :user OR m.user = :user")
    Page<Project> findAllForUserPaginated(@Param("user") User user, Pageable pageable);
}
