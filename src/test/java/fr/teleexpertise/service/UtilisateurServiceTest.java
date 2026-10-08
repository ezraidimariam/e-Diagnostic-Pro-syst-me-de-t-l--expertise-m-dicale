package fr.teleexpertise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import fr.teleexpertise.TeleexpertiseApplication;
import fr.teleexpertise.dao.UtilisateurDao;
import fr.teleexpertise.entity.Utilisateur;

@DataJpaTest
@Import(TeleexpertiseApplication.class)
class UtilisateurServiceTest {

    @Autowired
    private UtilisateurDao utilisateurDao;

    @Test
    void authentifier_should_return_user_when_credentials_are_valid() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername("formatrice");
        utilisateur.setPassword(encoder.encode("1234"));
        utilisateur.setRole("FORMATRICE");
        utilisateurDao.save(utilisateur);

        UtilisateurService service = new UtilisateurService(utilisateurDao);

        Utilisateur result = service.authentifier("formatrice", "1234");

        assertNotNull(result);
        assertEquals("FORMATRICE", result.getRole());
    }

    @Test
    void authentifier_should_throw_when_password_is_wrong() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername("formatrice");
        utilisateur.setPassword(encoder.encode("1234"));
        utilisateur.setRole("FORMATRICE");
        utilisateurDao.save(utilisateur);

        UtilisateurService service = new UtilisateurService(utilisateurDao);

        assertThrows(IllegalArgumentException.class, () -> service.authentifier("formatrice", "wrong-password"));
    }
}
