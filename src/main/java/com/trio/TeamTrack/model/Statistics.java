package com.trio.TeamTrack.model;

/**
 * POJO simple pour regrouper les statistiques d'un projet.
 * Pas d'annotation JPA : ce n'est pas une table en base de données,
 * c'est juste un objet qu'on calcule et qu'on envoie à la vue.
 */
public class Statistics {

    private int totalTasks;
    private int todoCount;
    private int inProgressCount;
    private int doneCount;
    private int progressPercentage;
    private long velocity; // nb de tâches terminées cette semaine

    // Constructeur vide obligatoire
    public Statistics() {}

    // Constructeur avec tous les paramètres
    public Statistics(int totalTasks, int todoCount, int inProgressCount,
                      int doneCount, long velocity) {
        this.totalTasks      = totalTasks;
        this.todoCount       = todoCount;
        this.inProgressCount = inProgressCount;
        this.doneCount       = doneCount;
        this.velocity        = velocity;
        // On calcule automatiquement le pourcentage
        this.progressPercentage = (totalTasks == 0) ? 0 : (doneCount * 100) / totalTasks;
    }

    // ---- Getters et Setters ----

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getTodoCount() { return todoCount; }
    public void setTodoCount(int todoCount) { this.todoCount = todoCount; }

    public int getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(int inProgressCount) { this.inProgressCount = inProgressCount; }

    public int getDoneCount() { return doneCount; }
    public void setDoneCount(int doneCount) { this.doneCount = doneCount; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public long getVelocity() { return velocity; }
    public void setVelocity(long velocity) { this.velocity = velocity; }
}
