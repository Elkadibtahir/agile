package com.trio.TeamTrack.repository;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.ProjectMember;
import com.trio.TeamTrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectAndUser(Project project, User user);
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    void deleteByProjectAndUser(Project project, User user);
}
