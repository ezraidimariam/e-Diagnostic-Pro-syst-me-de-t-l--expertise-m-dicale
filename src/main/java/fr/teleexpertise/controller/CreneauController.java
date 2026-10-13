package fr.teleexpertise.controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.teleexpertise.entity.Creneau;
import fr.teleexpertise.service.CreneauService;

@RestController
@RequestMapping("/api/creneaux")
public class CreneauController {

    private final CreneauService creneauService;

    public CreneauController(CreneauService creneauService) {
        this.creneauService = creneauService;
    }

    @GetMapping
    public List<Creneau> lister(@RequestParam Long medecinId,
            @RequestParam(defaultValue = "false") boolean disponibles, HttpServletRequest request) {
        verifierProfilMedecin(request, medecinId);
        return disponibles ? creneauService.listerCreneauxDisponibles(medecinId)
                : creneauService.listerParMedecin(medecinId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Creneau creer(@RequestBody CreneauRequest request, HttpServletRequest servletRequest) {
        verifierProfilMedecin(servletRequest, request.medecinId());
        return creneauService.creer(request.medecinId(), request.dateHeure());
    }

    @PostMapping("/planning")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Creneau> genererPlanning(@RequestBody PlanningRequest request, HttpServletRequest servletRequest) {
        verifierProfilMedecin(servletRequest, request.medecinId());
        return creneauService.genererPlanning(request.medecinId(), request.date());
    }

    @PostMapping("/{id}/reservation")
    public Creneau reserver(@PathVariable Long id, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        return "MEDECIN_SPECIALISTE".equals(user.role())
                ? creneauService.reserver(id, user.medecinId())
                : creneauService.reserver(id);
    }

    @DeleteMapping("/{id}/reservation")
    public Creneau annulerReservation(@PathVariable Long id, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        return "MEDECIN_SPECIALISTE".equals(user.role())
                ? creneauService.annulerReservation(id, user.medecinId())
                : creneauService.annulerReservation(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, HttpServletRequest request) {
        AuthController.SessionUser user = sessionUser(request);
        if ("MEDECIN_SPECIALISTE".equals(user.role())) {
            boolean appartientAuMedecin = creneauService.listerParMedecin(user.medecinId()).stream()
                    .filter(item -> item.getId().equals(id))
                    .findAny()
                    .isPresent();
            if (!appartientAuMedecin) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Ce créneau n'appartient pas à votre profil médecin.");
            }
        }
        creneauService.supprimer(id);
    }

    private void verifierProfilMedecin(HttpServletRequest request, Long medecinId) {
        AuthController.SessionUser user = sessionUser(request);
        if ("MEDECIN_SPECIALISTE".equals(user.role()) && !user.medecinId().equals(medecinId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Vous ne pouvez gérer que les créneaux de votre profil médecin.");
        }
    }

    private AuthController.SessionUser sessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (AuthController.SessionUser) session.getAttribute("utilisateurConnecte");
    }

    public record CreneauRequest(Long medecinId, LocalDateTime dateHeure) {
    }

    public record PlanningRequest(Long medecinId, LocalDate date) {
    }
}
