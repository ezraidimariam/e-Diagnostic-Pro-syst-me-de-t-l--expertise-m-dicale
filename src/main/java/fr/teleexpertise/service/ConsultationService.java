package fr.teleexpertise.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.ConsultationDao;
import fr.teleexpertise.dao.DemandeExpertiseDao;
import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.dao.PatientDao;
import fr.teleexpertise.entity.Consultation;
import fr.teleexpertise.entity.ActeTechnique;
import fr.teleexpertise.entity.DemandeExpertise;
import fr.teleexpertise.entity.Medecin;
import fr.teleexpertise.entity.Patient;

@Service
public class ConsultationService {

    private final ConsultationDao consultationDao;
    private final PatientDao patientDao;
    private final MedecinDao medecinDao;
    private final DemandeExpertiseDao demandeDao;

    public ConsultationService(ConsultationDao consultationDao, PatientDao patientDao, MedecinDao medecinDao,
            DemandeExpertiseDao demandeDao) {
        this.consultationDao = consultationDao;
        this.patientDao = patientDao;
        this.medecinDao = medecinDao;
        this.demandeDao = demandeDao;
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
            String diagnostic, String traitement, String statut, List<ActeTechnique> actesTechniques) {
        Consultation consultation = charger(id);
        if (symptomes == null || symptomes.isBlank()) {
            throw new IllegalArgumentException("Les symptômes sont obligatoires.");
        }
        if (statut != null && !java.util.Set.of(
                "EN_COURS", "EN_ATTENTE_AVIS_SPECIALISTE", "TERMINEE", "ANNULEE").contains(statut)) {
            throw new IllegalArgumentException("Le statut de la consultation n'est pas reconnu.");
        }
        if ("TERMINEE".equals(statut)
                && demandeDao.findByConsultation_IdOrderByIdDesc(id).stream()
                        .anyMatch(demande -> "EN_ATTENTE_AVIS_SPECIALISTE".equals(demande.getStatut()))) {
            throw new IllegalStateException(
                    "La consultation reste ouverte jusqu'à la réponse ou l'annulation de l'expertise.");
        }
        consultation.setSymptomes(symptomes.trim());
        consultation.setObservationsCliniques(observationsCliniques);
        consultation.setDiagnostic(diagnostic);
        consultation.setTraitement(traitement);
        if (statut != null) {
            consultation.setStatut(statut);
        }
        if (actesTechniques != null) {
            actesTechniques.forEach(this::validerActeTechnique);
            consultation.setActesTechniques(actesTechniques);
        }
        return consultationDao.save(consultation);
    }

    @Transactional
    public Consultation ajouterActeTechnique(Long id, String type, BigDecimal tarifDh) {
        Consultation consultation = charger(id);
        ActeTechnique acte = new ActeTechnique(type, tarifDh);
        validerActeTechnique(acte);
        consultation.getActesTechniques().add(acte);
        return consultationDao.save(consultation);
    }

    @Transactional(readOnly = true)
    public CoutConsultation calculerCout(Long id) {
        Consultation consultation = charger(id);
        BigDecimal actes = consultation.getActesTechniques().stream()
                .map(ActeTechnique::getTarifDh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expertises = demandeDao.findByConsultation_IdOrderByIdDesc(id).stream()
                .filter(demande -> !"ANNULEE".equals(demande.getStatut()))
                .map(DemandeExpertise::getMedecinSpecialiste)
                .filter(java.util.Objects::nonNull)
                .map(medecin -> BigDecimal.valueOf(medecin.getTarif()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CoutConsultation(consultation.getTarifDh(), expertises, actes,
                consultation.getTarifDh().add(expertises).add(actes));
    }

    private void validerActeTechnique(ActeTechnique acte) {
        Set<String> types = Set.of("Radiographie", "Échographie", "IRM", "Électrocardiogramme",
                "Acte dermatologique (laser)", "Fond d'œil", "Analyse de sang", "Analyse d'urine");
        if (acte == null || acte.getType() == null || !types.contains(acte.getType())
                || acte.getTarifDh() == null || acte.getTarifDh().signum() < 0) {
            throw new IllegalArgumentException("Le type et le tarif DH de l'acte médical sont invalides.");
        }
    }

    @Transactional
    public void supprimer(Long id) {
        Consultation consultation = charger(id);
        consultationDao.delete(consultation);
    }

    public record CoutConsultation(BigDecimal consultationDh, BigDecimal expertisesDh,
            BigDecimal actesTechniquesDh, BigDecimal totalDh) {
    }
}