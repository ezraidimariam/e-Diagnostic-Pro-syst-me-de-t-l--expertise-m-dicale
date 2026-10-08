package fr.teleexpertise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import fr.teleexpertise.TeleexpertiseApplication;
import fr.teleexpertise.dao.CreneauDao;
import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.entity.Creneau;
import fr.teleexpertise.entity.Medecin;

@DataJpaTest
@Import(TeleexpertiseApplication.class)
class CreneauServiceTest {

    @Autowired
    private CreneauDao creneauDao;

    @Autowired
    private MedecinDao medecinDao;

    @Test
    void listerCreneauxDisponibles_should_return_only_future_available_slots_in_order() {
        Medecin medecin = new Medecin();
        medecin.setNom("Dupont");
        medecin.setPrenom("Jean");
        medecin.setSpecialite("Cardiologie");
        medecin = medecinDao.save(medecin);

        Creneau creneau1 = new Creneau();
        creneau1.setMedecin(medecin);
        creneau1.setDateHeure(LocalDateTime.now().plusHours(1));
        creneau1.setDisponible(true);

        Creneau creneau2 = new Creneau();
        creneau2.setMedecin(medecin);
        creneau2.setDateHeure(LocalDateTime.now().plusHours(2));
        creneau2.setDisponible(true);

        Creneau creneauPasse = new Creneau();
        creneauPasse.setMedecin(medecin);
        creneauPasse.setDateHeure(LocalDateTime.now().minusHours(1));
        creneauPasse.setDisponible(true);

        Creneau creneauIndisponible = new Creneau();
        creneauIndisponible.setMedecin(medecin);
        creneauIndisponible.setDateHeure(LocalDateTime.now().plusHours(3));
        creneauIndisponible.setDisponible(false);

        Medecin autreMedecin = new Medecin();
        autreMedecin.setNom("Martin");
        autreMedecin.setPrenom("Alice");
        autreMedecin = medecinDao.save(autreMedecin);

        Creneau autreCreneau = new Creneau();
        autreCreneau.setMedecin(autreMedecin);
        autreCreneau.setDateHeure(LocalDateTime.now().plusHours(4));
        autreCreneau.setDisponible(true);

        creneauDao.saveAll(List.of(creneau1, creneau2, creneauPasse, creneauIndisponible, autreCreneau));

        CreneauService service = new CreneauService(creneauDao, medecinDao);

        List<Creneau> result = service.listerCreneauxDisponibles(medecin.getId());

        assertEquals(2, result.size());
        assertEquals(creneau1.getId(), result.get(0).getId());
        assertEquals(creneau2.getId(), result.get(1).getId());
    }

    @Test
    void reserver_should_mark_slot_as_unavailable() {
        Medecin medecin = new Medecin();
        medecin.setNom("Durand");
        medecin = medecinDao.save(medecin);

        Creneau creneau = new Creneau();
        creneau.setMedecin(medecin);
        creneau.setDateHeure(LocalDateTime.now().plusHours(2));
        creneau.setDisponible(true);
        creneau = creneauDao.save(creneau);

        CreneauService service = new CreneauService(creneauDao, medecinDao);

        Creneau result = service.reserver(creneau.getId());

        assertNotNull(result);
        assertFalse(result.isDisponible());
    }

    @Test
    void reserver_should_throw_when_slot_is_no_longer_available() {
        Medecin medecin = new Medecin();
        medecin.setNom("Bernard");
        medecin = medecinDao.save(medecin);

        Creneau creneau = new Creneau();
        creneau.setMedecin(medecin);
        creneau.setDateHeure(LocalDateTime.now().plusHours(1));
        creneau.setDisponible(false);
        creneau = creneauDao.save(creneau);

        CreneauService service = new CreneauService(creneauDao, medecinDao);
        Long creneauId = creneau.getId();

        assertThrows(IllegalStateException.class, () -> service.reserver(creneauId));
    }

    @Test
    void annulerReservation_should_set_slot_available_again() {
        Medecin medecin = new Medecin();
        medecin.setNom("Morel");
        medecin = medecinDao.save(medecin);

        Creneau creneau = new Creneau();
        creneau.setMedecin(medecin);
        creneau.setDateHeure(LocalDateTime.now().plusHours(1));
        creneau.setDisponible(false);
        creneau = creneauDao.save(creneau);

        CreneauService service = new CreneauService(creneauDao, medecinDao);

        Creneau result = service.annulerReservation(creneau.getId());

        assertNotNull(result);
        assertTrue(result.isDisponible());
    }
}
