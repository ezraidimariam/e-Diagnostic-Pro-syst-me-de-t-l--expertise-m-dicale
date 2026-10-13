package fr.teleexpertise.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiAuthenticationInterceptor implements HandlerInterceptor {

    private static final String USER_SESSION_KEY = "utilisateurConnecte";
    private static final String CSRF_SESSION_KEY = "csrfToken";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(USER_SESSION_KEY) instanceof AuthController.SessionUser)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Authentification requise.\"}");
            return false;
        }

        AuthController.SessionUser user =
                (AuthController.SessionUser) session.getAttribute(USER_SESSION_KEY);
        if (!autorise(user, request.getMethod(), request.getRequestURI()
                .substring(request.getContextPath().length()))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Votre rôle ne permet pas cette opération.\"}");
            return false;
        }

        if (!HttpMethod.GET.matches(request.getMethod())
                && !HttpMethod.HEAD.matches(request.getMethod())
                && !HttpMethod.OPTIONS.matches(request.getMethod())) {
            String expected = (String) session.getAttribute(CSRF_SESSION_KEY);
            String actual = request.getHeader("X-CSRF-TOKEN");
            if (expected == null || actual == null
                    || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                            actual.getBytes(StandardCharsets.UTF_8))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Jeton de sécurité absent ou invalide.\"}");
                return false;
            }
        }
        return true;
    }

    private boolean autorise(AuthController.SessionUser user, String method, String path) {
        String role = user.role();
        if ("/api/auth/logout".equals(path)) {
            return true;
        }
        if ("ADMIN".equals(role)) {
            return true;
        }
        if (("MEDECIN_GENERALISTE".equals(role) || "MEDECIN_SPECIALISTE".equals(role))
                && user.medecinId() == null) {
            return false;
        }
        boolean lecture = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        if (path.startsWith("/api/patients")) {
            return lecture && !"MEDECIN_SPECIALISTE".equals(role)
                    || ("INFIRMIER".equals(role)
                    && (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method)));
        }
        if (path.startsWith("/api/admissions")) {
            return (lecture && ("INFIRMIER".equals(role) || "MEDECIN_GENERALISTE".equals(role)))
                    || ("INFIRMIER".equals(role)
                    && (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method)));
        }
        if (path.startsWith("/api/medecins")) {
            return (lecture && !"INFIRMIER".equals(role))
                    || ("MEDECIN_SPECIALISTE".equals(role) && HttpMethod.PUT.matches(method));
        }
        if (path.startsWith("/api/consultations")) {
            return "MEDECIN_GENERALISTE".equals(role)
                    && (lecture || HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method));
        }
        if (path.startsWith("/api/creneaux")) {
            return ("MEDECIN_GENERALISTE".equals(role) || "MEDECIN_SPECIALISTE".equals(role))
                    && (lecture || ("MEDECIN_SPECIALISTE".equals(role)
                            && (HttpMethod.POST.matches(method) || HttpMethod.DELETE.matches(method))));
        }
        if (path.startsWith("/api/demandes-expertise")) {
            if (lecture) {
                return "MEDECIN_GENERALISTE".equals(role) || "MEDECIN_SPECIALISTE".equals(role);
            }
            if ("MEDECIN_GENERALISTE".equals(role)) {
                return HttpMethod.POST.matches(method)
                        && (path.equals("/api/demandes-expertise")
                                || path.matches("/api/demandes-expertise/\\d+/annulation"));
            }
            return "MEDECIN_SPECIALISTE".equals(role)
                    && HttpMethod.POST.matches(method)
                    && path.matches("/api/demandes-expertise/\\d+/reponse");
        }
        return false;
    }
}
