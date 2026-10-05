package fr.teleexpertise.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.teleexpertise.dao.CreneauDao;
import fr.teleexpertise.entity.Creneau;

@Service
public class CreneauService {

    private final CreneauDao creneauDao;

    public CreneauService(CreneauDao creneauDao) {
        this.creneauDao = creneauDao;
    }

    public List<Creneau> listerCreneauxDisponibles(Long medecinId) {
        return creneauDao.findByMedecin_IdAndDisponibleTrueAndDateHeureAfterOrderByDateHeureAsc(
                medecinId, LocalDateTime.now());
    }

    public Creneau reserver(Long creneauId) {
        Creneau creneau = chargerCreneau(creneauId);
        if (!creneau.isDisponible() || creneau.getDateHeure() == null
                || !creneau.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Ce créneau n'est plus disponible.");
        }

        creneau.setDisponible(false);
        return creneauDao.save(creneau);
    }

    public Creneau annulerReservation(Long creneauId) {
        Creneau creneau = chargerCreneau(creneauId);
        if (creneau.isDisponible() || creneau.getDateHeure() == null
                || !creneau.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cette réservation ne peut pas être annulée.");
        }

        creneau.setDisponible(true);
        return creneauDao.save(creneau);
    }

    private Creneau chargerCreneau(Long creneauId) {
        if (creneauId == null) {
            throw new IllegalArgumentException("L'identifiant du créneau est obligatoire.");
        }
        return creneauDao.findById(creneauId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable : " + creneauId));
    }
}