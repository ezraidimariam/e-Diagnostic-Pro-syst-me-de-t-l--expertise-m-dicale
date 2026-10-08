package fr.teleexpertise.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.teleexpertise.dao.UtilisateurDao;
import fr.teleexpertise.entity.Utilisateur;
import fr.teleexpertise.service.UtilisateurService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String USER_SESSION_KEY = "utilisateurConnecte";
    private static final String CSRF_SESSION_KEY = "csrfToken";

    private final UtilisateurDao utilisateurDao;
    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurDao utilisateurDao, UtilisateurService utilisateurService) {
        this.utilisateurDao = utilisateurDao;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/status")
    public AuthStatus status(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        SessionUser user = (SessionUser) session.getAttribute(USER_SESSION_KEY);
        String csrfToken = (String) session.getAttribute(CSRF_SESSION_KEY);
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute(CSRF_SESSION_KEY, csrfToken);
        }
        return new AuthStatus(utilisateurDao.count() > 0, user, csrfToken);
    }

    @PostMapping("/bootstrap")
    public SessionUser bootstrap(@RequestBody Credentials credentials, HttpServletRequest request) {
        try {
            Utilisateur user = utilisateurService.creerAdministrateurInitial(
                    credentials.nom(), credentials.prenom(), credentials.username(), credentials.password());
            return startSession(user, request);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @PostMapping("/login")
    public SessionUser login(@RequestBody Credentials credentials, HttpServletRequest request) {
        try {
            return startSession(
                    utilisateurService.authentifier(credentials.username(), credentials.password()), request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @PostMapping("/users")
    public SessionUser createUser(@RequestBody Credentials credentials, HttpServletRequest request) {
        SessionUser currentUser = currentUser(request);
        if (!"ADMIN".equals(currentUser.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès réservé à l'administrateur.");
        }
        Utilisateur user = utilisateurService.creerUtilisateur(credentials.nom(), credentials.prenom(),
                credentials.username(), credentials.password(), credentials.role());
        return toSessionUser(user);
    }

    @PostMapping("/logout")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private SessionUser startSession(Utilisateur user, HttpServletRequest request) {
        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        SessionUser sessionUser = toSessionUser(user);
        session.setAttribute(USER_SESSION_KEY, sessionUser);
        session.setAttribute(CSRF_SESSION_KEY, UUID.randomUUID().toString());
        return sessionUser;
    }

    private SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(USER_SESSION_KEY) instanceof SessionUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Veuillez vous connecter.");
        }
        return user;
    }

    private SessionUser toSessionUser(Utilisateur user) {
        return new SessionUser(user.getId(), user.getNom(), user.getPrenom(),
                user.getUsername(), user.getRole());
    }

    public record Credentials(String nom, String prenom, String username, String password, String role) {
    }

    public record SessionUser(Long id, String nom, String prenom, String username, String role) {
    }

    public record AuthStatus(boolean initialized, SessionUser user, String csrfToken) {
    }
}
