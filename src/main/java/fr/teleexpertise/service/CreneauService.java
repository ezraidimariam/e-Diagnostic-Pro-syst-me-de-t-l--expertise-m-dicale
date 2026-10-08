package fr.teleexpertise.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.CreneauDao;
import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.entity.Creneau;
import fr.teleexpertise.entity.Medecin;

@Service
public class CreneauService {

    private final CreneauDao creneauDao;
    private final MedecinDao medecinDao;

    @Autowired
    public CreneauService(CreneauDao creneauDao, MedecinDao medecinDao) {
        this.creneauDao = creneauDao;
        this.medecinDao = medecinDao;
    }

    @Transactional(readOnly = true)
    public List<Creneau> listerCreneauxDisponibles(Long medecinId) {
        return creneauDao.findByMedecin_IdAndDisponibleTrueAndDateHeureAfterOrderByDateHeureAsc(
                medecinId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Creneau> listerParMedecin(Long medecinId) {
        return creneauDao.findByMedecin_IdOrderByDateHeureAsc(medecinId);
    }

    @Transactional
    public Creneau creer(Long medecinId, LocalDateTime dateHeure) {
        if (dateHeure == null || !dateHeure.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date du créneau doit être dans le futur.");
        }
        Medecin medecin = medecinDao.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + medecinId));
        Creneau creneau = new Creneau();
        creneau.setMedecin(medecin);
        creneau.setDateHeure(dateHeure);
        creneau.setDisponible(true);
        return creneauDao.save(creneau);
    }

    @Transactional
    public Creneau reserver(Long creneauId) {
        return reserver(chargerCreneau(creneauId));
    }

    @Transactional
    public Creneau reserver(Long creneauId, Long medecinId) {
        Creneau creneau = chargerCreneau(creneauId);
        if (creneau.getMedecin() == null || !medecinId.equals(creneau.getMedecin().getId())) {
            throw new IllegalArgumentException("Le créneau ne correspond pas au médecin spécialiste.");
        }
        return reserver(creneau);
    }

    private Creneau reserver(Creneau creneau) {
        if (!creneau.isDisponible() || creneau.getDateHeure() == null
                || !creneau.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Ce créneau n'est plus disponible.");
        }

        creneau.setDisponible(false);
        return creneauDao.save(creneau);
    }

    @Transactional
    public Creneau annulerReservation(Long creneauId) {
        Creneau creneau = chargerCreneau(creneauId);
        if (creneau.isDisponible() || creneau.getDateHeure() == null
                || !creneau.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cette réservation ne peut pas être annulée.");
        }

        creneau.setDisponible(true);
        return creneauDao.save(creneau);
    }

    @Transactional
    public void supprimer(Long creneauId) {
        Creneau creneau = chargerCreneau(creneauId);
        if (!creneau.isDisponible()) {
            throw new IllegalStateException("Un créneau réservé ne peut pas être supprimé.");
        }
        creneauDao.delete(creneau);
    }

    private Creneau chargerCreneau(Long creneauId) {
        if (creneauId == null) {
            throw new IllegalArgumentException("L'identifiant du créneau est obligatoire.");
        }
        return creneauDao.findById(creneauId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable : " + creneauId));
    }
}