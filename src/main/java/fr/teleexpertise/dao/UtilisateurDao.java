package fr.teleexpertise.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Utilisateur;

public interface UtilisateurDao extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByUsername(String username);
}
