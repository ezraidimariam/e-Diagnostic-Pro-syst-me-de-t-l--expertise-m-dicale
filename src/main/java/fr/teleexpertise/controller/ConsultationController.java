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

import fr.teleexpertise.dao.ConsultationDao;
import fr.teleexpertise.entity.Consultation;
import fr.teleexpertise.service.ConsultationService;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationDao consultationDao;
    private final ConsultationService consultationService;

    public ConsultationController(ConsultationDao consultationDao, ConsultationService consultationService) {
        this.consultationDao = consultationDao;
        this.consultationService = consultationService;
    }

    @GetMapping
    public List<Consultation> lister(@RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String statut) {
        if (patientId != null) {
            return consultationDao.findByPatient_IdOrderByDateHeureDesc(patientId);
        }
        if (statut != null && !statut.isBlank()) {
            return consultationDao.findByStatut(statut);
        }
        return consultationService.lister();
    }

    @GetMapping("/{id}")
    public Consultation obtenir(@PathVariable Long id) {
        return consultationService.charger(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Consultation creer(@RequestBody ConsultationRequest request) {
        return consultationService.creerConsultation(request.patientId(), request.medecinGeneralisteId(),
                request.symptomes(), request.observationsCliniques());
    }

    @PutMapping("/{id}")
    public Consultation modifier(@PathVariable Long id, @RequestBody ConsultationUpdate request) {
        return consultationService.mettreAJour(id, request.symptomes(), request.observationsCliniques(),
                request.diagnostic(), request.traitement(), request.statut());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        consultationService.supprimer(id);
    }

    public record ConsultationRequest(Long patientId, Long medecinGeneralisteId,
            String symptomes, String observationsCliniques) {
    }

    public record ConsultationUpdate(String symptomes, String observationsCliniques,
            String diagnostic, String traitement, String statut) {
    }
}
