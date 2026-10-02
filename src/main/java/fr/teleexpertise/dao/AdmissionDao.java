package fr.teleexpertise.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Admission;

public interface AdmissionDao extends JpaRepository<Admission, Long> {

	List<Admission> findByStatutOrderByDateHeureAsc(String statut);
}