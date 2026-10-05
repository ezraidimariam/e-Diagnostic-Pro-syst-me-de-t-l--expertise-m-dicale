package fr.teleexpertise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import fr.teleexpertise.entity.Utilisateur;

class UtilisateurServiceTest {

    @Test
    void authentifier_should_return_user_when_credentials_are_valid() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername("formatrice");
        utilisateur.setPassword("1234");
        utilisateur.setRole("FORMATRICE");

        UtilisateurService service = new UtilisateurService(new StubUtilisateurDao(utilisateur));

        Utilisateur result = service.authentifier("formatrice", "1234");

        assertNotNull(result);
        assertEquals("FORMATRICE", result.getRole());
    }

    @Test
    void authentifier_should_throw_when_password_is_wrong() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername("formatrice");
        utilisateur.setPassword("1234");
        utilisateur.setRole("FORMATRICE");

        UtilisateurService service = new UtilisateurService(new StubUtilisateurDao(utilisateur));

        assertThrows(IllegalArgumentException.class, () -> service.authentifier("formatrice", "wrong-password"));
    }
}
