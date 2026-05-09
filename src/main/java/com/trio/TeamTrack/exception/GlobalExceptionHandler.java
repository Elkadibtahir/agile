package com.trio.TeamTrack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Intercepte toutes les exceptions non gérées dans l'application
 * et affiche une page d'erreur conviviale au lieu de la page blanche Spring.
 *
 * @ControllerAdvice = s'applique à TOUS les controllers automatiquement.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs métier (projet introuvable, membre déjà existant, etc.)
     * Ces erreurs viennent de nos services : throw new RuntimeException("...")
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("titre", "Oups, une erreur s'est produite");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("code", "400");
        return "error";
    }

    /**
     * Gère les accès à des ressources inexistantes (URL tapée à la main, etc.)
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model) {
        model.addAttribute("titre", "Page introuvable");
        model.addAttribute("message", "La page que vous cherchez n'existe pas ou a été supprimée.");
        model.addAttribute("code", "404");
        return "error";
    }

    /**
     * Gère toutes les autres exceptions inattendues (erreur serveur)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("titre", "Erreur interne du serveur");
        model.addAttribute("message", "Une erreur inattendue s'est produite. Veuillez réessayer.");
        model.addAttribute("code", "500");
        return "error";
    }
}
