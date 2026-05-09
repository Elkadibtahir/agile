package com.trio.TeamTrack.controller;

import com.trio.TeamTrack.entity.ProjectMember;
import com.trio.TeamTrack.enums.TaskStatus;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.model.Alert;
import com.trio.TeamTrack.model.Statistics;
import com.trio.TeamTrack.service.FileService;
import com.trio.TeamTrack.service.ProjectService;
import com.trio.TeamTrack.service.StatisticsService;
import com.trio.TeamTrack.service.TaskService;
import com.trio.TeamTrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired private ProjectService projectService;
    @Autowired private TaskService taskService;
    @Autowired private UserService userService;
    @Autowired private StatisticsService statisticsService;
    @Autowired private FileService fileService;

    @Value("${app.pagination.projects-per-page:9}")
    private int projectsPerPage;

    @Value("${app.pagination.tasks-per-page:10}")
    private int tasksPerPage;

    // Liste paginée des projets
    @GetMapping
    public String listProjects(@AuthenticationPrincipal UserDetails ud,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        User user = userService.findByUsername(ud.getUsername());
        Page<Project> projectPage = projectService.findAllForUserPaginated(user, page, projectsPerPage);

        model.addAttribute("projects",     projectPage.getContent());
        model.addAttribute("currentPage",  page);
        model.addAttribute("totalPages",   projectPage.getTotalPages());
        model.addAttribute("totalItems",   projectPage.getTotalElements());
        model.addAttribute("hasPrevious",  projectPage.hasPrevious());
        model.addAttribute("hasNext",      projectPage.hasNext());
        return "project/list";
    }

    @GetMapping("/new")
    public String newProjectForm() {
        return "project/form";
    }

    @PostMapping("/new")
    public String createProject(@RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes ra) {
        User owner = userService.findByUsername(ud.getUsername());
        Project p = projectService.createProject(name, description, startDate, endDate, owner);
        ra.addFlashAttribute("message", "Projet créé avec succès !");
        return "redirect:/projects/" + p.getId();
    }

    // Détail projet + Kanban + Stats + Alerte + pagination des tâches
    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails ud,
                                @RequestParam(defaultValue = "0") int page,
                                Model model) {
        User user    = userService.findByUsername(ud.getUsername());
        Project project = projectService.findById(id);

        Statistics stats = statisticsService.calculerStatistiques(project);
        Alert      alert = statisticsService.calculerAlerte(project);

        // Tâches paginées pour la liste complète
        var taskPage = taskService.findByProjectPaginated(project, page, tasksPerPage);

        model.addAttribute("project",         project);
        model.addAttribute("user",            user);
        model.addAttribute("stats",           stats);
        model.addAttribute("alert",           alert);
        model.addAttribute("todoTasks",       taskService.findByProjectAndStatus(project, TaskStatus.TODO));
        model.addAttribute("inProgressTasks", taskService.findByProjectAndStatus(project, TaskStatus.IN_PROGRESS));
        model.addAttribute("doneTasks",       taskService.findByProjectAndStatus(project, TaskStatus.DONE));
        model.addAttribute("allTasks",        taskPage.getContent());
        model.addAttribute("currentPage",     page);
        model.addAttribute("totalPages",      taskPage.getTotalPages());
        model.addAttribute("hasPrevious",     taskPage.hasPrevious());
        model.addAttribute("hasNext",         taskPage.hasNext());

        // Filtre les membres pour la sélection (exclut le Chef de Projet / Owner)
        var membresAAssigner = project.getMembers().stream()
                .filter(m -> !m.getUser().getId().equals(project.getOwner().getId()))
                .map(ProjectMember::getUser)
                .toList();

        model.addAttribute("eligibleUsers",   membresAAssigner);
        model.addAttribute("isOwner",         project.getOwner().getUsername().equals(ud.getUsername()));
        model.addAttribute("files",           fileService.getProjectFiles(project));
        return "project/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.findById(id));
        return "project/form";
    }

    @PostMapping("/{id}/edit")
    public String editProject(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              RedirectAttributes ra) {
        projectService.updateProject(id, name, description, startDate, endDate);
        ra.addFlashAttribute("message", "Projet mis à jour.");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes ra) {
        projectService.deleteProject(id);
        ra.addFlashAttribute("message", "Projet supprimé.");
        return "redirect:/projects";
    }

    @PostMapping("/{id}/membres/add")
    public String addMember(@PathVariable Long id,
                            @RequestParam Long userId,
                            @RequestParam(required = false) String role,
                            RedirectAttributes ra) {
        try {
            projectService.addMember(id, userId, role);
            ra.addFlashAttribute("message", "Rôle mis à jour !");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/membres/{userId}/remove")
    public String removeMember(@PathVariable Long id,
                               @PathVariable Long userId,
                               RedirectAttributes ra) {
        projectService.removeMember(id, userId);
        ra.addFlashAttribute("message", "Membre retiré.");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/join")
    public String joinProject(@RequestParam String code,
                              @AuthenticationPrincipal UserDetails ud,
                              RedirectAttributes ra) {
        try {
            User user = userService.findByUsername(ud.getUsername());
            projectService.joinByCode(code, user);
            ra.addFlashAttribute("message", "Vous avez rejoint le projet !");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/regenerate-code")
    public String regenerateCode(@PathVariable Long id, RedirectAttributes ra) {
        projectService.regenerateInvitationCode(id);
        ra.addFlashAttribute("message", "Nouveau code d'invitation généré !");
        return "redirect:/projects/" + id;
    }
}

