package com.trio.TeamTrack.service;

import com.trio.TeamTrack.entity.Comment;
import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.Task;
import com.trio.TeamTrack.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails.
 *
 * @Async : chaque méthode s'exécute dans un thread séparé.
 *          L'utilisateur n'attend PAS que l'email soit envoyé.
 *          Fonctionne grâce à @EnableAsync dans TeamTrackApplication.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    // -------------------------------------------------------
    // 1. Email de bienvenue à l'inscription
    // -------------------------------------------------------
    @Async
    public void envoyerEmailBienvenue(User user) {
        if (user.getEmail() == null) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(user.getEmail());
        msg.setSubject("[TeamTrack] Bienvenue !");
        msg.setText(
            "Bonjour " + nomAffiche(user) + ",\n\n" +
            "Votre compte TeamTrack a été créé avec succès !\n\n" +
            "Vous pouvez maintenant :\n" +
            "  • Créer et gérer vos projets Agile\n" +
            "  • Organiser vos tâches avec le tableau Kanban\n" +
            "  • Collaborer avec votre équipe\n" +
            "  • Suivre la progression en temps réel\n\n" +
            "Connectez-vous ici : http://localhost:8080/auth/login\n\n" +
            "— L'équipe TeamTrack"
        );
        envoyer(msg);
    }

    // -------------------------------------------------------
    // 2. Email quand une tâche est assignée
    //    Déclenché par : TaskService.createTask() et updateTask()
    // -------------------------------------------------------
    @Async
    public void envoyerNotificationAssignation(Task task, User assignee) {
        if (assignee == null || assignee.getEmail() == null) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(assignee.getEmail());
        msg.setSubject("[TeamTrack] Tâche assignée : " + task.getTitle());
        msg.setText(
            "Bonjour " + nomAffiche(assignee) + ",\n\n" +
            "Une tâche vous a été assignée sur TeamTrack.\n\n" +
            "📋 Tâche     : " + task.getTitle() + "\n" +
            "📁 Projet    : " + task.getProject().getName() + "\n" +
            "⚡ Priorité  : " + task.getPriority() + "\n" +
            "📊 Statut    : " + task.getStatus() + "\n" +
            (task.getDueDate() != null ? "📅 Échéance  : " + task.getDueDate() + "\n" : "") +
            (task.getDescription() != null && !task.getDescription().isBlank()
                ? "\n📝 Description :\n" + task.getDescription() + "\n"
                : "") +
            "\nConnectez-vous pour voir la tâche :\n" +
            "http://localhost:8080/projects/" + task.getProject().getId() + "\n\n" +
            "— L'équipe TeamTrack"
        );
        envoyer(msg);
    }

    // -------------------------------------------------------
    // 3. Email quand le statut d'une tâche change
    //    Déclenché par : TaskService.updateStatus()
    // -------------------------------------------------------
    @Async
    public void envoyerNotificationChangementStatut(Task task, User assignee) {
        if (assignee == null || assignee.getEmail() == null) return;

        String emoji = switch (task.getStatus()) {
            case DONE        -> "✅";
            case IN_PROGRESS -> "⚙️";
            case TODO        -> "📌";
        };

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(assignee.getEmail());
        msg.setSubject("[TeamTrack] Statut mis à jour : " + task.getTitle());
        msg.setText(
            "Bonjour " + nomAffiche(assignee) + ",\n\n" +
            "Le statut d'une tâche qui vous est assignée a changé.\n\n" +
            "📋 Tâche          : " + task.getTitle() + "\n" +
            "📁 Projet         : " + task.getProject().getName() + "\n" +
            emoji + "  Nouveau statut : " + task.getStatus() + "\n\n" +
            "http://localhost:8080/projects/" + task.getProject().getId() + "\n\n" +
            "— L'équipe TeamTrack"
        );
        envoyer(msg);
    }

    // -------------------------------------------------------
    // 4. Email quand un commentaire est ajouté sur une tâche
    //    Déclenché par : CommentService.addComment()
    //    Envoie à : l'assigné de la tâche (si différent de l'auteur)
    // -------------------------------------------------------
    @Async
    public void envoyerNotificationCommentaire(Comment comment, Task task) {
        User assignee = task.getAssignee();
        if (assignee == null || assignee.getEmail() == null) return;
        // Ne pas notifier si l'auteur est l'assigné lui-même
        if (assignee.getId().equals(comment.getAuthor().getId())) return;

        String sentimentEmoji = switch (comment.getSentiment()) {
            case "POSITIVE" -> "😊";
            case "NEGATIVE" -> "😟";
            default         -> "💬";
        };

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(assignee.getEmail());
        msg.setSubject("[TeamTrack] Nouveau commentaire sur : " + task.getTitle());
        msg.setText(
            "Bonjour " + nomAffiche(assignee) + ",\n\n" +
            nomAffiche(comment.getAuthor()) + " a commenté votre tâche.\n\n" +
            "📋 Tâche : " + task.getTitle() + "\n" +
            "📁 Projet : " + task.getProject().getName() + "\n\n" +
            sentimentEmoji + " Commentaire :\n" +
            "\"" + comment.getContent() + "\"\n\n" +
            "http://localhost:8080/projects/" + task.getProject().getId()
            + "/tasks/" + task.getId() + "\n\n" +
            "— L'équipe TeamTrack"
        );
        envoyer(msg);
    }

    // -------------------------------------------------------
    // 5. Email d'alerte de retard à l'owner du projet
    //    Peut être appelé manuellement ou planifié (@Scheduled)
    // -------------------------------------------------------
    @Async
    public void envoyerAlerteRetard(Project project, User owner, String message) {
        if (owner == null || owner.getEmail() == null) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(owner.getEmail());
        msg.setSubject("[TeamTrack] ⚠️ Alerte retard : " + project.getName());
        msg.setText(
            "Bonjour " + nomAffiche(owner) + ",\n\n" +
            "Une alerte a été détectée sur votre projet.\n\n" +
            "📁 Projet  : " + project.getName() + "\n" +
            "📅 Fin prévue : " + (project.getEndDate() != null ? project.getEndDate() : "Non définie") + "\n\n" +
            "⚠️ " + message + "\n\n" +
            "Consultez le tableau de bord pour agir :\n" +
            "http://localhost:8080/projects/" + project.getId() + "\n\n" +
            "— L'équipe TeamTrack"
        );
        envoyer(msg);
    }

    // -------------------------------------------------------
    // Méthode utilitaire : envoi sécurisé (catch sans crash)
    // -------------------------------------------------------
    private void envoyer(SimpleMailMessage msg) {
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            // On log l'erreur SANS bloquer l'application
            System.err.println("[EmailService] Échec envoi email vers "
                + msg.getTo()[0] + " : " + e.getMessage());
        }
    }

    // Retourne le prénom ou le username selon ce qui est disponible
    private String nomAffiche(User user) {
        return user.getFullName() != null ? user.getFullName() : user.getUsername();
    }
}
