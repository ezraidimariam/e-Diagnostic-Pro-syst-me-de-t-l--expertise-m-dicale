package fr.teleexpertise.controller;

import java.time.LocalDateTime;
import java.util.List;

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
            @RequestParam(defaultValue = "false") boolean disponibles) {
        return disponibles ? creneauService.listerCreneauxDisponibles(medecinId)
                : creneauService.listerParMedecin(medecinId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Creneau creer(@RequestBody CreneauRequest request) {
        return creneauService.creer(request.medecinId(), request.dateHeure());
    }

    @PostMapping("/{id}/reservation")
    public Creneau reserver(@PathVariable Long id) {
        return creneauService.reserver(id);
    }

    @DeleteMapping("/{id}/reservation")
    public Creneau annulerReservation(@PathVariable Long id) {
        return creneauService.annulerReservation(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        creneauService.supprimer(id);
    }

    public record CreneauRequest(Long medecinId, LocalDateTime dateHeure) {
    }
}
