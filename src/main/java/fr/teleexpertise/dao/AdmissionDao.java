package fr.teleexpertise.dao;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Admission;

public interface AdmissionDao extends JpaRepository<Admission, Long> {

    List<Admission> findAllByOrderByDateHeureAsc();

    List<Admission> findByStatutOrderByDateHeureAsc(String statut);

    List<Admission> findByDateHeureGreaterThanEqualAndDateHeureLessThanOrderByDateHeureAsc(
            LocalDateTime debut, LocalDateTime fin);

    List<Admission> findByStatutAndDateHeureGreaterThanEqualAndDateHeureLessThanOrderByDateHeureAsc(
            String statut, LocalDateTime debut, LocalDateTime fin);
}