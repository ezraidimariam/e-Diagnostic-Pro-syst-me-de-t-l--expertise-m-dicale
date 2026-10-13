package fr.teleexpertise.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.UtilisateurDao;
import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.entity.Medecin;
import fr.teleexpertise.entity.Utilisateur;

@Service
public class UtilisateurService {

    private final UtilisateurDao utilisateurDao;
    private final MedecinDao medecinDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurDao utilisateurDao) {
        this.utilisateurDao = utilisateurDao;
        this.medecinDao = null;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Autowired
    public UtilisateurService(UtilisateurDao utilisateurDao, MedecinDao medecinDao) {
        this.utilisateurDao = utilisateurDao;
        this.medecinDao = medecinDao;
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
        return creerUtilisateur(nom, prenom, username, password, role, null);
    }

    @Transactional
    public Utilisateur creerUtilisateur(String nom, String prenom, String username,
            String password, String role, Long medecinId) {
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
        Medecin medecin = null;
        boolean roleMedecin = "MEDECIN_GENERALISTE".equals(roleNormalise)
                || "MEDECIN_SPECIALISTE".equals(roleNormalise);
        if (roleMedecin) {
            if (medecinId == null || medecinDao == null) {
                throw new IllegalArgumentException("Un profil médecin doit être associé à ce compte.");
            }
            medecin = medecinDao.findById(medecinId)
                    .orElseThrow(() -> new IllegalArgumentException("Profil médecin introuvable : " + medecinId));
        } else if (medecinId != null) {
            throw new IllegalArgumentException("Seul un compte médecin peut être lié à un profil médecin.");
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
        utilisateur.setMedecin(medecin);

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
