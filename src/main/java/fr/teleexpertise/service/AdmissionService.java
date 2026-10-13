package fr.teleexpertise.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.AdmissionDao;
import fr.teleexpertise.dao.PatientDao;
import fr.teleexpertise.entity.Admission;
import fr.teleexpertise.entity.Patient;

@Service
public class AdmissionService {

    private static final Set<String> STATUTS = Set.of(
            "EN_ATTENTE_MEDECIN_GENERALISTE", "EN_COURS", "ORIENTEE_SPECIALISTE", "TERMINEE", "ANNULEE");

    private final AdmissionDao admissionDao;
    private final PatientDao patientDao;

    public AdmissionService(AdmissionDao admissionDao, PatientDao patientDao) {
        this.admissionDao = admissionDao;
        this.patientDao = patientDao;
    }

    @Transactional
    public Admission creer(Long patientId, String infirmier, String tensionArterielle,
            int frequenceCardiaque, double temperature, int frequenceRespiratoire,
            Double poids, Double taille) {
        if (patientId == null) {
            throw new IllegalArgumentException("Le patient est obligatoire.");
        }
        Patient patient = patientDao.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + patientId));
        return creer(patient, infirmier, tensionArterielle, frequenceCardiaque, temperature,
                frequenceRespiratoire, poids, taille);
    }

    @Transactional
    public Admission accueillir(Long patientId, String nom, String prenom, LocalDate dateNaissance,
            String numeroSecuriteSociale, String telephone, String adresse, String mutuelle,
            String antecedents, String allergies, String traitementsEnCours, String infirmier,
            String tensionArterielle, int frequenceCardiaque, double temperature,
            int frequenceRespiratoire, Double poids, Double taille) {
        Patient patient;
        if (patientId != null) {
            patient = patientDao.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + patientId));
        } else {
            if (numeroSecuriteSociale == null || numeroSecuriteSociale.isBlank()) {
                throw new IllegalArgumentException("Le numéro de sécurité sociale est obligatoire.");
            }
            String numeroNormalise = numeroSecuriteSociale.trim();
            patient = patientDao.findByNumeroSecuriteSocialeIgnoreCase(numeroNormalise).orElse(null);
            if (patient == null) {
                if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()
                        || dateNaissance == null || dateNaissance.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "Le nom, le prénom et une date de naissance valide sont obligatoires pour un nouveau patient.");
                }
                patient = new Patient();
                patient.setNom(nom.trim());
                patient.setPrenom(prenom.trim());
                patient.setDateNaissance(dateNaissance);
                patient.setNumeroSecuriteSociale(numeroNormalise);
                patient.setTelephone(telephone);
                patient.setAdresse(adresse);
                patient.setMutuelle(mutuelle);
                patient.setAntecedents(antecedents);
                patient.setAllergies(allergies);
                patient.setTraitementsEnCours(traitementsEnCours);
                patient = patientDao.save(patient);
            }
        }
        return creer(patient, infirmier, tensionArterielle, frequenceCardiaque, temperature,
                frequenceRespiratoire, poids, taille);
    }

    private Admission creer(Patient patient, String infirmier, String tensionArterielle,
            int frequenceCardiaque, double temperature, int frequenceRespiratoire,
            Double poids, Double taille) {
        if (infirmier == null || infirmier.isBlank() || tensionArterielle == null
                || tensionArterielle.isBlank() || frequenceCardiaque <= 0
                || temperature <= 0 || frequenceRespiratoire <= 0
                || (poids != null && poids <= 0) || (taille != null && taille <= 0)) {
            throw new IllegalArgumentException("Les informations de triage doivent être renseignées et valides.");
        }
        Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setInfirmier(infirmier.trim());
        admission.setTensionArterielle(tensionArterielle.trim());
        admission.setFrequenceCardiaque(frequenceCardiaque);
        admission.setTemperature(temperature);
        admission.setFrequenceRespiratoire(frequenceRespiratoire);
        admission.setPoids(poids);
        admission.setTaille(taille);
        admission.setDateHeure(LocalDateTime.now());
        admission.setStatut("EN_ATTENTE_MEDECIN_GENERALISTE");
        return admissionDao.save(admission);
    }

    @Transactional(readOnly = true)
    public List<Admission> lister(String statut) {
        return lister(statut, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Admission> lister(String statut, LocalDate date) {
        LocalDate jour = date == null ? LocalDate.now() : date;
        LocalDateTime debut = jour.atStartOfDay();
        LocalDateTime fin = jour.plusDays(1).atStartOfDay();
        if (statut == null || statut.isBlank()) {
            return admissionDao.findByDateHeureGreaterThanEqualAndDateHeureLessThanOrderByDateHeureAsc(
                    debut, fin);
        }
        return admissionDao.findByStatutAndDateHeureGreaterThanEqualAndDateHeureLessThanOrderByDateHeureAsc(
                statut, debut, fin);
    }

    @Transactional(readOnly = true)
    public Admission charger(Long id) {
        return admissionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admission introuvable : " + id));
    }

    @Transactional
    public Admission changerStatut(Long id, String statut) {
        if (statut == null || !STATUTS.contains(statut)) {
            throw new IllegalArgumentException("Le statut de l'admission n'est pas reconnu.");
        }
        Admission admission = charger(id);
        admission.setStatut(statut);
        return admissionDao.save(admission);
    }
}
