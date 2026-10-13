package fr.teleexpertise.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import fr.teleexpertise.entity.ActeTechnique;
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
            @RequestParam(required = false) String statut, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        if ("MEDECIN_GENERALISTE".equals(user.role())) {
            return consultationService.lister().stream()
                    .filter(consultation -> consultation.getMedecinGeneraliste() != null
                            && user.medecinId().equals(consultation.getMedecinGeneraliste().getId()))
                    .filter(consultation -> patientId == null || consultation.getPatient().getId().equals(patientId))
                    .filter(consultation -> statut == null || statut.isBlank()
                            || statut.equals(consultation.getStatut()))
                    .toList();
        }
        if (patientId != null) {
            return consultationDao.findByPatient_IdOrderByDateHeureDesc(patientId);
        }
        if (statut != null && !statut.isBlank()) {
            return consultationDao.findByStatut(statut);
        }
        return consultationService.lister();
    }

    @GetMapping("/{id}")
    public Consultation obtenir(@PathVariable Long id, HttpServletRequest request) {
        Consultation consultation = consultationService.charger(id);
        verifierProprietaire(consultation, sessionUser(request));
        return consultation;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Consultation creer(@RequestBody ConsultationRequest request, HttpServletRequest servletRequest) {
        AuthController.SessionUser user = sessionUser(servletRequest);
        if ("MEDECIN_GENERALISTE".equals(user.role())
                && !user.medecinId().equals(request.medecinGeneralisteId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez créer une consultation qu'avec votre profil médecin.");
        }
        return consultationService.creerConsultation(request.patientId(), request.medecinGeneralisteId(),
                request.symptomes(), request.observationsCliniques());
    }

    @PutMapping("/{id}")
    public Consultation modifier(@PathVariable Long id, @RequestBody ConsultationUpdate request,
            HttpServletRequest servletRequest) {
        verifierProprietaire(consultationService.charger(id), sessionUser(servletRequest));
        return consultationService.mettreAJour(id, request.symptomes(), request.observationsCliniques(),
                request.diagnostic(), request.traitement(), request.statut(), request.actesTechniques());
    }

    @GetMapping("/{id}/cout")
    public ConsultationService.CoutConsultation cout(@PathVariable Long id, HttpServletRequest request) {
        verifierProprietaire(consultationService.charger(id), sessionUser(request));
        return consultationService.calculerCout(id);
    }

    @PostMapping("/{id}/actes-techniques")
    public Consultation ajouterActeTechnique(@PathVariable Long id, @RequestBody ActeTechniqueRequest request,
            HttpServletRequest servletRequest) {
        verifierProprietaire(consultationService.charger(id), sessionUser(servletRequest));
        return consultationService.ajouterActeTechnique(id, request.type(), request.tarifDh());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        consultationService.supprimer(id);
    }

    private AuthController.SessionUser sessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (AuthController.SessionUser) session.getAttribute("utilisateurConnecte");
    }

    private void verifierProprietaire(Consultation consultation, AuthController.SessionUser user) {
        if ("MEDECIN_GENERALISTE".equals(user.role())
                && (consultation.getMedecinGeneraliste() == null || user.medecinId() == null
                        || !user.medecinId().equals(consultation.getMedecinGeneraliste().getId()))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cette consultation n'appartient pas à votre profil médecin.");
        }
    }

    public record ConsultationRequest(Long patientId, Long medecinGeneralisteId,
            String symptomes, String observationsCliniques) {
    }

    public record ConsultationUpdate(String symptomes, String observationsCliniques,
            String diagnostic, String traitement, String statut, List<ActeTechnique> actesTechniques) {
    }

    public record ActeTechniqueRequest(String type, java.math.BigDecimal tarifDh) {
    }
}
