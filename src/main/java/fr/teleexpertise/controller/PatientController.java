package fr.teleexpertise.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.teleexpertise.dao.PatientDao;
import fr.teleexpertise.entity.Patient;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientDao patientDao;

    public PatientController(PatientDao patientDao) {
        this.patientDao = patientDao;
    }

    @GetMapping
    public List<Patient> lister(@RequestParam(required = false) String numeroSecuriteSociale) {
        if (numeroSecuriteSociale != null && !numeroSecuriteSociale.isBlank()) {
            return patientDao.findByNumeroSecuriteSocialeContainingIgnoreCase(numeroSecuriteSociale.trim());
        }
        return patientDao.findAll();
    }

    @GetMapping("/{id}")
    public Patient obtenir(@PathVariable Long id) {
        return charger(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient creer(@RequestBody Patient patient) {
        valider(patient);
        patient.setId(null);
        if (patient.getNumeroSecuriteSociale() != null) {
            patient.setNumeroSecuriteSociale(patient.getNumeroSecuriteSociale().trim());
        }
        validerNumeroSecuriteSocialeUnique(patient);
        return patientDao.save(patient);
    }

    @PutMapping("/{id}")
    public Patient modifier(@PathVariable Long id, @RequestBody Patient donnees) {
        Patient patient = charger(id);
        valider(donnees);
        donnees.setId(id);
        if (donnees.getNumeroSecuriteSociale() != null) {
            donnees.setNumeroSecuriteSociale(donnees.getNumeroSecuriteSociale().trim());
        }
        validerNumeroSecuriteSocialeUnique(donnees);
        patient.setNom(donnees.getNom().trim());
        patient.setPrenom(donnees.getPrenom().trim());
        patient.setDateNaissance(donnees.getDateNaissance());
        patient.setTelephone(donnees.getTelephone());
        patient.setAdresse(donnees.getAdresse());
        patient.setNumeroSecuriteSociale(donnees.getNumeroSecuriteSociale());
        patient.setMutuelle(donnees.getMutuelle());
        patient.setAntecedents(donnees.getAntecedents());
        patient.setAllergies(donnees.getAllergies());
        patient.setTraitementsEnCours(donnees.getTraitementsEnCours());
        return patientDao.save(patient);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        patientDao.delete(charger(id));
    }

    private Patient charger(Long id) {
        return patientDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + id));
    }

    private void valider(Patient patient) {
        if (patient.getNom() == null || patient.getNom().isBlank()
                || patient.getPrenom() == null || patient.getPrenom().isBlank()
                || patient.getDateNaissance() == null
                || patient.getNumeroSecuriteSociale() == null
                || patient.getNumeroSecuriteSociale().isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom, le prénom, la date de naissance et le numéro de sécurité sociale sont obligatoires.");
        }
        if (patient.getDateNaissance() != null && patient.getDateNaissance().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur.");
        }
    }

    private void validerNumeroSecuriteSocialeUnique(Patient patient) {
        String numero = patient.getNumeroSecuriteSociale();
        if (numero == null || numero.isBlank()) {
            return;
        }
        boolean duplicate = patientDao.findByNumeroSecuriteSocialeIgnoreCase(numero.trim())
                .filter(existing -> !existing.getId().equals(patient.getId()))
                .isPresent();
        if (duplicate) {
            throw new IllegalArgumentException("Un patient possède déjà ce numéro de sécurité sociale.");
        }
    }
}
