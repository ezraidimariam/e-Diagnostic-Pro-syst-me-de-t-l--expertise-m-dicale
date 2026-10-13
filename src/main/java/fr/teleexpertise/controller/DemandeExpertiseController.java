package fr.teleexpertise.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) Long consultationId, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        return demandeService.lister(statut, priorite, consultationId, user.role(), user.medecinId());
    }

    @GetMapping("/{id}")
    public DemandeExpertise obtenir(@PathVariable Long id, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        return demandeService.lister(null, null, null, user.role(), user.medecinId()).stream()
                .filter(demande -> demande.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Demande d'expertise introuvable."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeExpertise creer(@RequestBody DemandeRequest request, HttpServletRequest servletRequest) {
        AuthController.SessionUser user = sessionUser(servletRequest);
        return demandeService.creer(request.consultationId(), request.medecinSpecialisteId(),
                request.creneauId(), request.question(), request.priorite(), request.modeEchange(),
                "MEDECIN_GENERALISTE".equals(user.role()) ? user.medecinId() : null);
    }

    @PostMapping("/{id}/reponse")
    public DemandeExpertise repondre(@PathVariable Long id, @RequestBody AnswerRequest request,
            HttpServletRequest servletRequest) {
        AuthController.SessionUser user = sessionUser(servletRequest);
        return demandeService.repondre(id, request.avisSpecialiste(), request.recommandations(),
                "MEDECIN_SPECIALISTE".equals(user.role()) ? user.medecinId() : null);
    }

    @PostMapping("/{id}/annulation")
    public DemandeExpertise annuler(@PathVariable Long id, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        return demandeService.annuler(id,
                "MEDECIN_GENERALISTE".equals(user.role()) ? user.medecinId() : null);
    }

    private AuthController.SessionUser sessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (AuthController.SessionUser) session.getAttribute("utilisateurConnecte");
    }

    public record DemandeRequest(Long consultationId, Long medecinSpecialisteId, Long creneauId,
            String question, String priorite, String modeEchange) {
    }

    public record AnswerRequest(String avisSpecialiste, String recommandations) {
    }
}
