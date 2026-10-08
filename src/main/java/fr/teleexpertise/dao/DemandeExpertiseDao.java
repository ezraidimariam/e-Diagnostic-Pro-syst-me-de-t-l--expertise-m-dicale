package fr.teleexpertise.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.DemandeExpertise;

public interface DemandeExpertiseDao extends JpaRepository<DemandeExpertise, Long> {

    List<DemandeExpertise> findByStatut(String statut);

    List<DemandeExpertise> findByConsultation_IdOrderByIdDesc(Long consultationId);
}