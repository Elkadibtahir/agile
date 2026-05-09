package com.trio.TeamTrack.service;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.ProjectMember;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.repository.ProjectMemberRepository;
import com.trio.TeamTrack.repository.ProjectRepository;
import com.trio.TeamTrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    public Project createProject(String name, String description,
                                 LocalDate startDate, LocalDate endDate, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setOwner(owner);

        // Génération d'un code d'invitation unique (8 caractères)
        project.setInvitationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        project.setInvitationCodeCreatedAt(LocalDateTime.now());

        project = projectRepository.save(project);

        // L'owner est automatiquement membre "Chef de Projet"
        ProjectMember membre = new ProjectMember();
        membre.setProject(project);
        membre.setUser(owner);
        membre.setRole("Chef de Projet");
        memberRepository.save(membre);

        return project;
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable."));
    }

    // Liste simple (dashboard)
    public List<Project> findAllForUser(User user) {
        return projectRepository.findAllForUser(user);
    }

    // Liste paginée (page /projects)
    public Page<Project> findAllForUserPaginated(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.findAllForUserPaginated(user, pageable);
    }

    public void updateProject(Long id, String name, String description,
                              LocalDate startDate, LocalDate endDate) {
        Project project = findById(id);
        project.setName(name);
        project.setDescription(description);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public void addMember(Long projectId, Long userId, String role) {
        Project project = findById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        // Empêcher d'attribuer le rôle "Chef de Projet" manuellement
        if ("Chef de Projet".equalsIgnoreCase(role)) {
            throw new RuntimeException("Le rôle 'Chef de Projet' est réservé au créateur du projet.");
        }

        ProjectMember membre = memberRepository.findByProjectAndUser(project, user)
                .orElse(new ProjectMember());

        membre.setProject(project);
        membre.setUser(user);
        membre.setRole((role == null || role.trim().isEmpty()) ? "Membre" : role);
        memberRepository.save(membre);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = findById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        memberRepository.deleteByProjectAndUser(project, user);
    }

    public void joinByCode(String code, User user) {
        Project project = projectRepository.findByInvitationCode(code)
                .orElseThrow(() -> new RuntimeException("Code d'invitation invalide."));

        // Vérification de l'expiration (24 heures)
        if (project.getInvitationCodeCreatedAt() != null &&
            project.getInvitationCodeCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Ce code d'invitation a expiré (validité 24h). Demandez au Chef de Projet d'en générer un nouveau.");
        }

        if (memberRepository.existsByProjectAndUser(project, user)) {
            throw new RuntimeException("Vous êtes déjà membre de ce projet.");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole("Membre");
        memberRepository.save(member);
    }

    public void regenerateInvitationCode(Long projectId) {
        Project project = findById(projectId);
        project.setInvitationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        project.setInvitationCodeCreatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }
}
