package fr.teleexpertise.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.teleexpertise.dao.UtilisateurDao;
import fr.teleexpertise.entity.Utilisateur;

@Service
public class UtilisateurService {

    private final UtilisateurDao utilisateurDao;

    public UtilisateurService(UtilisateurDao utilisateurDao) {
        this.utilisateurDao = utilisateurDao;
    }

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

        if (!Objects.equals(utilisateur.getPassword(), password)) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        return utilisateur;
    }

    public Utilisateur creerUtilisateur(String nom, String prenom, String username,
            String password, String role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }

        Optional<Utilisateur> utilisateurExistant = utilisateurDao.findByUsername(username.trim());
        if (utilisateurExistant.isPresent()) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setUsername(username.trim());
        utilisateur.setPassword(password);
        utilisateur.setRole(role.trim().toUpperCase());
        utilisateur.setActif(true);

        return utilisateurDao.save(utilisateur);
    }
}
