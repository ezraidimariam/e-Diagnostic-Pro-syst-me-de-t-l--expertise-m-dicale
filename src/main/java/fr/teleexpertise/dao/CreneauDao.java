package fr.teleexpertise.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Creneau;

public interface CreneauDao extends JpaRepository<Creneau, Long> {

    List<Creneau> findByMedecin_IdOrderByDateHeureAsc(Long medecinId);

    List<Creneau> findByMedecin_IdAndDisponibleTrueAndDateHeureAfterOrderByDateHeureAsc(
            Long medecinId, LocalDateTime maintenant);
}