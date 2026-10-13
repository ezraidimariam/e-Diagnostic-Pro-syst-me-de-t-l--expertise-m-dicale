package fr.teleexpertise.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.teleexpertise.entity.Admission;
import fr.teleexpertise.service.AdmissionService;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping
    public List<Admission> lister(@RequestParam(required = false) String statut,
            @RequestParam(required = false) LocalDate date) {
        return admissionService.lister(statut, date);
    }

    @GetMapping("/{id}")
    public Admission obtenir(@PathVariable Long id) {
        return admissionService.charger(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Admission creer(@RequestBody AdmissionRequest request) {
        return admissionService.accueillir(request.patientId(), request.nom(), request.prenom(),
                request.dateNaissance(), request.numeroSecuriteSociale(), request.telephone(),
                request.adresse(), request.mutuelle(), request.antecedents(), request.allergies(),
                request.traitementsEnCours(), request.infirmier(), request.tensionArterielle(),
                request.frequenceCardiaque(), request.temperature(), request.frequenceRespiratoire(),
                request.poids(), request.taille());
    }

    @PutMapping("/{id}/statut")
    public Admission changerStatut(@PathVariable Long id, @RequestBody StatusRequest request) {
        return admissionService.changerStatut(id, request.statut());
    }

    public record AdmissionRequest(Long patientId, String nom, String prenom, LocalDate dateNaissance,
            String numeroSecuriteSociale, String telephone, String adresse, String mutuelle,
            String antecedents, String allergies, String traitementsEnCours,
            String infirmier, String tensionArterielle, int frequenceCardiaque,
            double temperature, int frequenceRespiratoire, Double poids, Double taille) {
    }

    public record StatusRequest(String statut) {
    }
}
