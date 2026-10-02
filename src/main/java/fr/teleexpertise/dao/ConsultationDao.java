package fr.teleexpertise.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Consultation;

public interface ConsultationDao extends JpaRepository<Consultation, Long> {

    List<Consultation> findByStatut(String statut);
}