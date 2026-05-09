package com.trio.TeamTrack.controller;

import com.trio.TeamTrack.dto.RegisterDTO;
import com.trio.TeamTrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null)  model.addAttribute("error", "Identifiants incorrects.");
        if (logout != null) model.addAttribute("message", "Vous êtes déconnecté.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        // On envoie un DTO vide pour le formulaire Thymeleaf
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    /**
     * Traite l'inscription.
     *
     * @Valid          : déclenche la validation du RegisterDTO
     * BindingResult  : contient les erreurs de validation
     *
     * Si le formulaire est invalide, on reaffiche la page avec les erreurs.
     * Si valide, on inscrit l'utilisateur et on redirige vers le login.
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes ra) {

        // S'il y a des erreurs de validation, on reste sur la page
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            ra.addFlashAttribute("message", "Compte créé ! Un email de bienvenue vous a été envoyé.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}

