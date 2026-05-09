package com.trio.TeamTrack.model;

/**
 * POJO simple pour représenter une alerte de retard.
 * On crée cet objet dans StatisticsService et on l'envoie à la vue Thymeleaf.
 */
public class Alert {

    // Constantes pour les niveaux d'alerte
    public static final String ON_TRACK = "ON_TRACK"; // Tout va bien
    public static final String WARNING   = "WARNING";  // Attention
    public static final String AT_RISK   = "AT_RISK";  // En danger
    public static final String NO_DATE   = "NO_DATE";  // Pas de date de fin

    private String level;        // Le niveau parmi les constantes ci-dessus
    private String message;      // Message à afficher à l'utilisateur
    private long velocity;       // Tâches terminées cette semaine
    private long remainingTasks; // Tâches qui restent à faire

    public Alert() {}

    public Alert(String level, String message, long velocity, long remainingTasks) {
        this.level         = level;
        this.message       = message;
        this.velocity      = velocity;
        this.remainingTasks = remainingTasks;
    }

    // Méthodes utilitaires pour Thymeleaf (th:if="${alert.onTrack}")
    public boolean isOnTrack() { return ON_TRACK.equals(level); }
    public boolean isWarning()  { return WARNING.equals(level); }
    public boolean isAtRisk()   { return AT_RISK.equals(level); }
    public boolean isNoDate()   { return NO_DATE.equals(level); }

    // ---- Getters et Setters ----

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getVelocity() { return velocity; }
    public void setVelocity(long velocity) { this.velocity = velocity; }

    public long getRemainingTasks() { return remainingTasks; }
    public void setRemainingTasks(long remainingTasks) { this.remainingTasks = remainingTasks; }
}
