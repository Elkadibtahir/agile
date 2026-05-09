package com.trio.TeamTrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) pour le formulaire d'inscription.
 *
 * On utilise des annotations de validation Jakarta :
 * - @NotBlank  : le champ ne doit pas être vide
 * - @Size      : longueur min/max
 * - @Email     : format email valide
 * - @Pattern   : expression régulière
 *
 * Spring valide automatiquement quand on met @Valid dans le controller.
 */
public class RegisterDTO {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire.")
    @Size(min = 3, max = 30, message = "Le nom d'utilisateur doit faire entre 3 et 30 caractères.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscore.")
    private String username;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "Format d'email invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre."
    )
    private String password;

    @NotBlank(message = "Le nom complet est obligatoire.")
    @Size(min = 2, max = 100, message = "Le nom complet doit faire entre 2 et 100 caractères.")
    private String fullName;

    // Getters et Setters

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
