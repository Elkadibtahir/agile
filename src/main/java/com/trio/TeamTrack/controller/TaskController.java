package com.trio.TeamTrack.controller;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.Task;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.enums.TaskPriority;
import com.trio.TeamTrack.enums.TaskStatus;
import com.trio.TeamTrack.service.CommentService;
import com.trio.TeamTrack.service.ProjectService;
import com.trio.TeamTrack.service.TaskService;
import com.trio.TeamTrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    // Formulaire nouvelle tâche
    @GetMapping("/new")
    public String newTaskForm(@PathVariable Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        model.addAttribute("project",    project);
        model.addAttribute("priorities", TaskPriority.values());
        return "task/form";
    }

    // Créer une tâche
    @PostMapping("/new")
    public String createTask(@PathVariable Long projectId,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam TaskPriority priority,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                              @RequestParam(required = false) Long assigneeId,
                              RedirectAttributes ra) {
        Project project = projectService.findById(projectId);
        taskService.createTask(title, description, priority, dueDate, project, assigneeId);
        ra.addFlashAttribute("message", "Tâche créée !");
        return "redirect:/projects/" + projectId;
    }

    // Détail d'une tâche avec commentaires
    @GetMapping("/{taskId}")
    public String taskDetail(@PathVariable Long projectId,
                              @PathVariable Long taskId,
                              @AuthenticationPrincipal UserDetails ud,
                              Model model) {
        Task task    = taskService.findById(taskId);
        User user    = userService.findByUsername(ud.getUsername());
        Project project = projectService.findById(projectId);

        model.addAttribute("task",       task);
        model.addAttribute("project",    project);
        model.addAttribute("user",       user);
        model.addAttribute("comments",   commentService.findByTask(task));
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("statuses",   TaskStatus.values());

        return "task/detail";
    }

    // Changer le statut (boutons du Kanban)
    @PostMapping("/{taskId}/status")
    public String updateStatus(@PathVariable Long projectId,
                                @PathVariable Long taskId,
                                @RequestParam TaskStatus status) {
        taskService.updateStatus(taskId, status);
        return "redirect:/projects/" + projectId;
    }

    // Version AJAX pour le Drag & Drop
    @PostMapping("/{taskId}/status/ajax")
    @ResponseBody
    public ResponseEntity<String> updateStatusAjax(@PathVariable Long taskId,
                                                   @RequestParam TaskStatus status) {
        taskService.updateStatus(taskId, status);
        return ResponseEntity.ok("Statut mis à jour");
    }

    // Modifier une tâche
    @PostMapping("/{taskId}/edit")
    public String editTask(@PathVariable Long projectId,
                            @PathVariable Long taskId,
                            @RequestParam String title,
                            @RequestParam(required = false) String description,
                            @RequestParam TaskPriority priority,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                            @RequestParam TaskStatus status,
                            @RequestParam(required = false) Long assigneeId,
                            RedirectAttributes ra) {
        taskService.updateTask(taskId, title, description, priority, dueDate, status, assigneeId);
        ra.addFlashAttribute("message", "Tâche mise à jour.");
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Supprimer une tâche
    @PostMapping("/{taskId}/delete")
    public String deleteTask(@PathVariable Long projectId,
                              @PathVariable Long taskId,
                              RedirectAttributes ra) {
        taskService.deleteTask(taskId);
        ra.addFlashAttribute("message", "Tâche supprimée.");
        return "redirect:/projects/" + projectId;
    }

    // Ajouter un commentaire
    @PostMapping("/{taskId}/comments")
    public String addComment(@PathVariable Long projectId,
                              @PathVariable Long taskId,
                              @RequestParam String content,
                              @AuthenticationPrincipal UserDetails ud) {
        Task task   = taskService.findById(taskId);
        User author = userService.findByUsername(ud.getUsername());
        commentService.addComment(content, task, author);
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }

    // Supprimer un commentaire
    @PostMapping("/{taskId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long projectId,
                                 @PathVariable Long taskId,
                                 @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return "redirect:/projects/" + projectId + "/tasks/" + taskId;
    }
}
