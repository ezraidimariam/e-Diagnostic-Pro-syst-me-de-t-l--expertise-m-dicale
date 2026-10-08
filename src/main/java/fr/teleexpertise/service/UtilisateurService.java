package fr.teleexpertise.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.UtilisateurDao;
import fr.teleexpertise.entity.Utilisateur;

@Service
public class UtilisateurService {

    private final UtilisateurDao utilisateurDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurDao utilisateurDao) {
        this.utilisateurDao = utilisateurDao;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(readOnly = true)
    public Utilisateur authentifier(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        Utilisateur utilisateur = utilisateurDao.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Nom d'utilisateur introuvable."));

        if (!utilisateur.isActif()) {
            throw new IllegalArgumentException("Ce compte est désactivé.");
        }

        if (!passwordEncoder.matches(password, utilisateur.getPassword())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        return utilisateur;
    }

    @Transactional
    public Utilisateur creerUtilisateur(String nom, String prenom, String username,
            String password, String role) {
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException("Le nom et le prénom sont obligatoires.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        String roleNormalise = role.trim().toUpperCase();
        if (!java.util.Set.of("ADMIN", "MEDECIN_GENERALISTE", "MEDECIN_SPECIALISTE", "INFIRMIER")
                .contains(roleNormalise)) {
            throw new IllegalArgumentException("Le rôle n'est pas reconnu.");
        }

        Optional<Utilisateur> utilisateurExistant = utilisateurDao.findByUsername(username.trim());
        if (utilisateurExistant.isPresent()) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setUsername(username.trim());
        utilisateur.setPassword(passwordEncoder.encode(password));
        utilisateur.setRole(roleNormalise);
        utilisateur.setActif(true);

        return utilisateurDao.save(utilisateur);
    }

    @Transactional
    public Utilisateur creerAdministrateurInitial(String nom, String prenom, String username,
            String password) {
        if (utilisateurDao.count() != 0) {
            throw new IllegalStateException("Le compte administrateur est déjà configuré.");
        }
        return creerUtilisateur(nom, prenom, username, password, "ADMIN");
    }
}
