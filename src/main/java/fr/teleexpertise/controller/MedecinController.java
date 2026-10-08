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

import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.entity.Medecin;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    private final MedecinDao medecinDao;

    public MedecinController(MedecinDao medecinDao) {
        this.medecinDao = medecinDao;
    }

    @GetMapping
    public List<Medecin> lister(@RequestParam(required = false) String specialite) {
        if (specialite != null && !specialite.isBlank()) {
            return medecinDao.rechercherParSpecialiteTrieeParTarif(specialite.trim());
        }
        return medecinDao.findAll();
    }

    @GetMapping("/{id}")
    public Medecin obtenir(@PathVariable Long id) {
        return charger(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medecin creer(@RequestBody Medecin medecin) {
        valider(medecin);
        medecin.setId(null);
        return medecinDao.save(medecin);
    }

    @PutMapping("/{id}")
    public Medecin modifier(@PathVariable Long id, @RequestBody Medecin donnees) {
        Medecin medecin = charger(id);
        valider(donnees);
        medecin.setNom(donnees.getNom().trim());
        medecin.setPrenom(donnees.getPrenom().trim());
        medecin.setSpecialite(donnees.getSpecialite().trim());
        medecin.setTarif(donnees.getTarif());
        medecin.setDisponible(donnees.isDisponible());
        return medecinDao.save(medecin);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        medecinDao.delete(charger(id));
    }

    private Medecin charger(Long id) {
        return medecinDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + id));
    }

    private void valider(Medecin medecin) {
        if (medecin.getNom() == null || medecin.getNom().isBlank()
                || medecin.getPrenom() == null || medecin.getPrenom().isBlank()
                || medecin.getSpecialite() == null || medecin.getSpecialite().isBlank()
                || !Double.isFinite(medecin.getTarif()) || medecin.getTarif() < 0) {
            throw new IllegalArgumentException("Les informations du médecin sont invalides.");
        }
    }
}
