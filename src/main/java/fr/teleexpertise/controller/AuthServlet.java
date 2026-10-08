package fr.teleexpertise.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import fr.teleexpertise.entity.Utilisateur;
import fr.teleexpertise.service.UtilisateurService;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/auth/login"})
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UtilisateurService utilisateurService;

    @Override
    public void init() {
        WebApplicationContext context = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        this.utilisateurService = context.getBean(UtilisateurService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Utilisateur utilisateur = utilisateurService.authentifier(username, password);
            request.getSession().setAttribute("utilisateurConnecte", utilisateur);
            request.setAttribute("utilisateur", utilisateur);
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("erreur", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
