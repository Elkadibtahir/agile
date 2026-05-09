package com.trio.TeamTrack.service;

import com.trio.TeamTrack.dto.RegisterDTO;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Inscription avec validation via RegisterDTO.
     * Le DTO a déjà été validé par @Valid dans le controller,
     * mais on vérifie aussi l'unicité en base ici.
     */
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());

        userRepository.save(user);

        // Email de bienvenue envoyé en arrière-plan
        emailService.envoyerEmailBienvenue(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Version paginée pour les listes d'utilisateurs
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
