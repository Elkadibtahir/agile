package com.trio.TeamTrack.service;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.Task;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.enums.TaskPriority;
import com.trio.TeamTrack.enums.TaskStatus;
import com.trio.TeamTrack.repository.TaskRepository;
import com.trio.TeamTrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService; // pour notifier l'assigné

    // Créer une tâche et notifier l'assigné par email
    public Task createTask(String title, String description, TaskPriority priority,
                           LocalDate dueDate, Project project, Long assigneeId) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus(TaskStatus.TODO);
        task.setProject(project);

        if (assigneeId != null) {
            userRepository.findById(assigneeId).ifPresent(assignee -> {
                task.setAssignee(assignee);
            });
        }

        Task saved = taskRepository.save(task);

        // Notification email si un assigné est défini
        if (saved.getAssignee() != null) {
            emailService.envoyerNotificationAssignation(saved, saved.getAssignee());
        }

        return saved;
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable."));
    }

    public List<Task> findByProject(Project project) {
        return taskRepository.findByProject(project);
    }

    public List<Task> findByProjectAndStatus(Project project, TaskStatus status) {
        return taskRepository.findByProjectAndStatus(project, status);
    }

    /**
     * Version paginée des tâches d'un projet.
     * page = numéro de page (commence à 0)
     * size = nombre de tâches par page
     */
    public Page<Task> findByProjectPaginated(Project project, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending().and(Sort.by("dueDate").ascending()));
        return taskRepository.findByProject(project, pageable);
    }

    // Changer le statut (Kanban) + notification email
    public void updateStatus(Long taskId, TaskStatus newStatus) {
        Task task = findById(taskId);
        task.setStatus(newStatus);
        taskRepository.save(task);

        // Notification si la tâche a un assigné
        if (task.getAssignee() != null) {
            emailService.envoyerNotificationChangementStatut(task, task.getAssignee());
        }
    }

    // Modifier une tâche + notifier si l'assigné a changé
    public void updateTask(Long id, String title, String description,
                           TaskPriority priority, LocalDate dueDate,
                           TaskStatus status, Long assigneeId) {
        Task task = findById(id);
        User ancienAssigne = task.getAssignee();

        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus(status);

        if (assigneeId != null) {
            userRepository.findById(assigneeId).ifPresent(task::setAssignee);
        } else {
            task.setAssignee(null);
        }

        taskRepository.save(task);

        // Si l'assigné a changé, on notifie le nouvel assigné
        User nouvelAssigne = task.getAssignee();
        if (nouvelAssigne != null && !nouvelAssigne.equals(ancienAssigne)) {
            emailService.envoyerNotificationAssignation(task, nouvelAssigne);
        }
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
