package com.trio.TeamTrack.service;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.Task;
import com.trio.TeamTrack.enums.TaskStatus;
import com.trio.TeamTrack.model.Alert;
import com.trio.TeamTrack.model.Statistics;
import com.trio.TeamTrack.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service qui calcule :
 * - Les statistiques d'un projet (nb tâches, progression, vélocité)
 * - L'alerte prédictive de retard
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Calcule les statistiques d'un projet.
     */
    public Statistics calculerStatistiques(Project project) {
        List<Task> taches = taskRepository.findByProject(project);

        int total      = taches.size();
        int todo       = 0;
        int inProgress = 0;
        int done       = 0;

        // On parcourt toutes les tâches et on compte par statut
        for (Task t : taches) {
            if (t.getStatus() == TaskStatus.TODO)        todo++;
            else if (t.getStatus() == TaskStatus.IN_PROGRESS) inProgress++;
            else if (t.getStatus() == TaskStatus.DONE)   done++;
        }

        // Vélocité = tâches terminées dans les 7 derniers jours
        LocalDateTime ilYa7Jours = LocalDateTime.now().minusDays(7);
        long velocity = taskRepository.countCompletedSince(project, ilYa7Jours);

        return new Statistics(total, todo, inProgress, done, velocity);
    }

    /**
     * Calcule l'alerte prédictive de retard.
     * Compare la vélocité actuelle aux tâches restantes et au temps disponible.
     */
    public Alert calculerAlerte(Project project) {
        // Pas de date de fin → impossible de prédire
        if (project.getEndDate() == null) {
            return new Alert(Alert.NO_DATE, "Aucune date de fin définie.", 0, 0);
        }

        Statistics stats = calculerStatistiques(project);
        long restantes = stats.getTodoCount() + stats.getInProgressCount();

        // Toutes les tâches sont terminées
        if (restantes == 0) {
            return new Alert(Alert.ON_TRACK, "🎉 Projet terminé !", stats.getVelocity(), 0);
        }

        long velocity = stats.getVelocity();

        // Aucune tâche terminée cette semaine → danger immédiat
        if (velocity == 0) {
            return new Alert(Alert.AT_RISK,
                "🚨 Aucune tâche terminée cette semaine. Projet en danger !",
                0, restantes);
        }

        // Combien de semaines reste-t-il avant la date de fin ?
        long joursRestants = LocalDate.now().until(project.getEndDate()).getDays();
        double semainesRestantes = joursRestants / 7.0;
        double tachesEstimees = velocity * semainesRestantes;

        if (tachesEstimees >= restantes) {
            return new Alert(Alert.ON_TRACK,
                "✅ Projet sur la bonne voie ! Vélocité : " + velocity + " tâches/semaine.",
                velocity, restantes);

        } else if (tachesEstimees >= restantes * 0.7) {
            return new Alert(Alert.WARNING,
                "⚠️ Progression ralentit. Vélocité : " + velocity + " tâches/semaine.",
                velocity, restantes);

        } else {
            return new Alert(Alert.AT_RISK,
                "🚨 Risque de retard ! Vélocité insuffisante : " + velocity + " tâches/semaine.",
                velocity, restantes);
        }
    }
}
