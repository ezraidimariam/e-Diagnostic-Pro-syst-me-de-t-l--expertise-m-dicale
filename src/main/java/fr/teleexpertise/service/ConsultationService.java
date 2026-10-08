package fr.teleexpertise.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.ConsultationDao;
import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.dao.PatientDao;
import fr.teleexpertise.entity.Consultation;
import fr.teleexpertise.entity.Medecin;
import fr.teleexpertise.entity.Patient;

@Service
public class ConsultationService {

    private final ConsultationDao consultationDao;
    private final PatientDao patientDao;
    private final MedecinDao medecinDao;

    public ConsultationService(ConsultationDao consultationDao, PatientDao patientDao, MedecinDao medecinDao) {
        this.consultationDao = consultationDao;
        this.patientDao = patientDao;
        this.medecinDao = medecinDao;
    }

    @Transactional
    public Consultation creerConsultation(Patient patient, Medecin medecinGeneraliste,
            String symptomes, String observationsCliniques) {
        if (patient == null || patient.getId() == null || medecinGeneraliste == null
                || medecinGeneraliste.getId() == null) {
            throw new IllegalArgumentException("Le patient et le médecin sont obligatoires.");
        }
        if (symptomes == null || symptomes.isBlank()) {
            throw new IllegalArgumentException("Les symptômes sont obligatoires.");
        }
        Consultation consultation = new Consultation();
        consultation.setDateHeure(LocalDateTime.now());
        consultation.setPatient(patient);
        consultation.setMedecinGeneraliste(medecinGeneraliste);
        consultation.setSymptomes(symptomes);
        consultation.setObservationsCliniques(observationsCliniques);

        return consultationDao.save(consultation);
    }

    @Transactional
    public Consultation creerConsultation(Long patientId, Long medecinId, String symptomes,
            String observationsCliniques) {
        if (patientId == null || medecinId == null) {
            throw new IllegalArgumentException("Le patient et le médecin sont obligatoires.");
        }
        Patient patient = patientDao.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + patientId));
        Medecin medecin = medecinDao.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + medecinId));
        return creerConsultation(patient, medecin, symptomes, observationsCliniques);
    }

    @Transactional(readOnly = true)
    public Consultation charger(Long id) {
        return consultationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public java.util.List<Consultation> lister() {
        return consultationDao.findAll();
    }

    @Transactional
    public Consultation mettreAJour(Long id, String symptomes, String observationsCliniques,
            String diagnostic, String traitement, String statut) {
        Consultation consultation = charger(id);
        if (symptomes == null || symptomes.isBlank()) {
            throw new IllegalArgumentException("Les symptômes sont obligatoires.");
        }
        if (statut != null && !java.util.Set.of(
                "EN_COURS", "EN_ATTENTE_AVIS_SPECIALISTE", "TERMINEE", "ANNULEE").contains(statut)) {
            throw new IllegalArgumentException("Le statut de la consultation n'est pas reconnu.");
        }
        consultation.setSymptomes(symptomes.trim());
        consultation.setObservationsCliniques(observationsCliniques);
        consultation.setDiagnostic(diagnostic);
        consultation.setTraitement(traitement);
        if (statut != null) {
            consultation.setStatut(statut);
        }
        return consultationDao.save(consultation);
    }

    @Transactional
    public void supprimer(Long id) {
        Consultation consultation = charger(id);
        consultationDao.delete(consultation);
    }
}