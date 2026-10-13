package fr.teleexpertise.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Patient;

public interface PatientDao extends JpaRepository<Patient, Long> {

    java.util.List<Patient> findByNumeroSecuriteSocialeContainingIgnoreCase(String numeroSecuriteSociale);

    java.util.Optional<Patient> findByNumeroSecuriteSocialeIgnoreCase(String numeroSecuriteSociale);
}