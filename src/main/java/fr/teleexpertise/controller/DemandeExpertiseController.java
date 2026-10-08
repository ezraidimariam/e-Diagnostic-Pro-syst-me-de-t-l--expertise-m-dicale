package fr.teleexpertise.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.teleexpertise.entity.DemandeExpertise;
import fr.teleexpertise.service.DemandeExpertiseService;

@RestController
@RequestMapping("/api/demandes-expertise")
public class DemandeExpertiseController {

    private final DemandeExpertiseService demandeService;

    public DemandeExpertiseController(DemandeExpertiseService demandeService) {
        this.demandeService = demandeService;
    }

    @GetMapping
    public List<DemandeExpertise> lister(@RequestParam(required = false) String statut,
            @RequestParam(required = false) Long consultationId) {
        return demandeService.lister(statut, consultationId);
    }

    @GetMapping("/{id}")
    public DemandeExpertise obtenir(@PathVariable Long id) {
        return demandeService.charger(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeExpertise creer(@RequestBody DemandeRequest request) {
        return demandeService.creer(request.consultationId(), request.medecinSpecialisteId(),
                request.creneauId(), request.question(), request.priorite(), request.modeEchange());
    }

    @PostMapping("/{id}/reponse")
    public DemandeExpertise repondre(@PathVariable Long id, @RequestBody AnswerRequest request) {
        return demandeService.repondre(id, request.avisSpecialiste());
    }

    @PostMapping("/{id}/annulation")
    public DemandeExpertise annuler(@PathVariable Long id) {
        return demandeService.annuler(id);
    }

    public record DemandeRequest(Long consultationId, Long medecinSpecialisteId, Long creneauId,
            String question, String priorite, String modeEchange) {
    }

    public record AnswerRequest(String avisSpecialiste) {
    }
}
