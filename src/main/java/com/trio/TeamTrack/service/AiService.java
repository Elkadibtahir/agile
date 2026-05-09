package com.trio.TeamTrack.service;

import org.springframework.stereotype.Service;

/**
 * Service d'intelligence artificielle simple.
 * Analyse le sentiment d'un texte (commentaire) par mots-clés.
 * Pas d'API externe - tout est en Java pur, facile à comprendre.
 */
@Service
public class AiService {

    // Mots qui indiquent un sentiment positif
    private static final String[] MOTS_POSITIFS = {
        "bien", "super", "excellent", "parfait", "bravo", "terminé", "fini",
        "réussi", "bon", "génial", "rapide", "ok", "cool", "facile", "clair",
        "merci", "great", "good", "done", "nice", "fixed", "completed", "thanks"
    };

    // Mots qui indiquent un sentiment négatif
    private static final String[] MOTS_NEGATIFS = {
        "bug", "erreur", "problème", "bloqué", "impossible", "cassé", "retard",
        "lent", "mauvais", "difficile", "urgent", "souci", "panne", "manque",
        "fail", "error", "broken", "stuck", "issue", "crash", "problem"
    };

    /**
     * Analyse le sentiment d'un texte.
     * Retourne "POSITIVE", "NEGATIVE" ou "NEUTRAL"
     */
    public String analyserSentiment(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return "NEUTRAL";
        }

        // On met en minuscules et on découpe en mots
        String[] mots = texte.toLowerCase().split("\\s+");
        int score = 0;

        for (String mot : mots) {
            // On supprime la ponctuation du mot
            String motPropre = mot.replaceAll("[^a-zA-Zàâéèêëîïôùûüç]", "");

            // Vérification dans les mots positifs
            for (String positif : MOTS_POSITIFS) {
                if (motPropre.equals(positif)) {
                    score++;
                    break;
                }
            }

            // Vérification dans les mots négatifs
            for (String negatif : MOTS_NEGATIFS) {
                if (motPropre.equals(negatif)) {
                    score--;
                    break;
                }
            }
        }

        if (score > 0)  return "POSITIVE";
        if (score < 0)  return "NEGATIVE";
        return "NEUTRAL";
    }

    /**
     * Retourne l'emoji correspondant au sentiment.
     */
    public String getEmoji(String sentiment) {
        if ("POSITIVE".equals(sentiment)) return "😊";
        if ("NEGATIVE".equals(sentiment)) return "😟";
        return "😐";
    }
}
