package fr.teleexpertise.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import fr.teleexpertise.dao.ConsultationDao;
import fr.teleexpertise.entity.Consultation;
import fr.teleexpertise.entity.Medecin;
import fr.teleexpertise.entity.Patient;

@Service
public class ConsultationService {

    private final ConsultationDao consultationDao;

    public ConsultationService(ConsultationDao consultationDao) {
        this.consultationDao = consultationDao;
    }

    public Consultation creerConsultation(Patient patient, Medecin medecinGeneraliste,
            String symptomes, String observationsCliniques) {
        Consultation consultation = new Consultation();
        consultation.setDateHeure(LocalDateTime.now());
        consultation.setPatient(patient);
        consultation.setMedecinGeneraliste(medecinGeneraliste);
        consultation.setSymptomes(symptomes);
        consultation.setObservationsCliniques(observationsCliniques);

        return consultationDao.save(consultation);
    }
}