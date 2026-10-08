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
}
