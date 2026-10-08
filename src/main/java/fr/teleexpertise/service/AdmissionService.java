package fr.teleexpertise.service;

import java.time.LocalDateTime;
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
            double poids, double taille) {
        if (patientId == null) {
            throw new IllegalArgumentException("Le patient est obligatoire.");
        }
        Patient patient = patientDao.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + patientId));
        if (infirmier == null || infirmier.isBlank() || tensionArterielle == null
                || tensionArterielle.isBlank() || frequenceCardiaque <= 0
                || temperature <= 0 || frequenceRespiratoire <= 0 || poids <= 0 || taille <= 0) {
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
        if (statut == null || statut.isBlank()) {
            return admissionDao.findAllByOrderByDateHeureAsc();
        }
        return admissionDao.findByStatutOrderByDateHeureAsc(statut);
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
