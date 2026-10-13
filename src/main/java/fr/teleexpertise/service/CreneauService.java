package fr.teleexpertise.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Transactional
    public List<Creneau> listerParMedecin(Long medecinId) {
        List<Creneau> creneaux = creneauDao.findByMedecin_IdOrderByDateHeureAsc(medecinId);
        boolean changed = false;
        for (Creneau creneau : creneaux) {
            if (creneau.getDateHeure() != null && !creneau.getDateHeure().isAfter(LocalDateTime.now())
                    && !creneau.isArchive()) {
                creneau.setArchive(true);
                changed = true;
            }
        }
        if (changed) {
            creneauDao.saveAll(creneaux);
        }
        return creneaux;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void archiverCreneauxPasses() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Creneau> creneaux = creneauDao.findAll();
        List<Creneau> aArchiver = creneaux.stream()
                .filter(creneau -> creneau.getDateHeure() != null
                        && !creneau.getDateHeure().isAfter(maintenant) && !creneau.isArchive())
                .peek(creneau -> creneau.setArchive(true))
                .toList();
        if (!aArchiver.isEmpty()) {
            creneauDao.saveAll(aArchiver);
        }
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
        creneau.setDureeMinutes(30);
        return creneauDao.save(creneau);
    }

    @Transactional
    public List<Creneau> genererPlanning(Long medecinId, LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date du planning doit être aujourd'hui ou dans le futur.");
        }
        Medecin medecin = medecinDao.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + medecinId));
        List<Creneau> existants = creneauDao.findByMedecin_IdOrderByDateHeureAsc(medecinId);
        List<Creneau> resultat = new ArrayList<>();
        for (LocalTime heure = LocalTime.of(9, 0); heure.isBefore(LocalTime.NOON); heure = heure.plusMinutes(30)) {
            LocalDateTime dateHeure = LocalDateTime.of(date, heure);
            if (!dateHeure.isAfter(LocalDateTime.now())) {
                continue;
            }
            boolean dejaCree = existants.stream()
                    .anyMatch(creneau -> dateHeure.equals(creneau.getDateHeure()));
            if (!dejaCree) {
                Creneau creneau = new Creneau();
                creneau.setMedecin(medecin);
                creneau.setDateHeure(dateHeure);
                creneau.setDureeMinutes(30);
                creneau.setDisponible(!LocalTime.of(10, 30).equals(heure));
                resultat.add(creneau);
            }
        }
        return creneauDao.saveAll(resultat);
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
        creneau.setArchive(false);
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
    public Creneau annulerReservation(Long creneauId, Long medecinId) {
        Creneau creneau = chargerCreneau(creneauId);
        verifierMedecin(creneau, medecinId);
        return annulerReservation(creneauId);
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

    private void verifierMedecin(Creneau creneau, Long medecinId) {
        if (creneau.getMedecin() == null || medecinId == null
                || !medecinId.equals(creneau.getMedecin().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Ce créneau n'appartient pas à votre profil médecin.");
        }
    }
}