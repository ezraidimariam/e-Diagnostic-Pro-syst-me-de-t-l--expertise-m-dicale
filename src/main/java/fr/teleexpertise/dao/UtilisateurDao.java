package fr.teleexpertise.dao;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.entity.Utilisateur;

public class UtilisateurDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateur.getId() == null) {
            entityManager.persist(utilisateur);
            return utilisateur;
        }
        return entityManager.merge(utilisateur);
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> findByUsername(String username) {
        return entityManager
                .createQuery("SELECT u FROM Utilisateur u WHERE u.username = :username", Utilisateur.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Utilisateur.class, id));
    }

    @Transactional(readOnly = true)
    public java.util.List<Utilisateur> findAll() {
        return entityManager.createQuery("SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom",
                Utilisateur.class).getResultList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return entityManager.createQuery("SELECT COUNT(u) FROM Utilisateur u", Long.class)
                .getSingleResult();
    }
}
