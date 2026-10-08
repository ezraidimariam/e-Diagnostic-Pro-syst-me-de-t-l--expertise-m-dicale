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
    public List<Patient> lister() {
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
        return patientDao.save(patient);
    }

    @PutMapping("/{id}")
    public Patient modifier(@PathVariable Long id, @RequestBody Patient donnees) {
        Patient patient = charger(id);
        valider(donnees);
        patient.setNom(donnees.getNom().trim());
        patient.setPrenom(donnees.getPrenom().trim());
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
                || patient.getPrenom() == null || patient.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le nom et le prénom du patient sont obligatoires.");
        }
    }
}
