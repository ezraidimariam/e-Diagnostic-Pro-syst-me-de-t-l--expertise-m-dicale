<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Tableau de bord</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f3f6fb; margin: 0; padding: 40px; }
        .box { max-width: 600px; margin: 80px auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
        h2 { margin-top: 0; }
        .info { background: #edf4ff; padding: 15px; border-radius: 8px; }
    </style>
</head>
<body>
<div class="box">
    <h2>Bienvenue</h2>
    <div class="info">
        <p>Utilisateur connecté : <strong>${utilisateur.username}</strong></p>
        <p>Rôle : <strong>${utilisateur.role}</strong></p>
    </div>
    <p><a href="${pageContext.request.contextPath}/login">Retour à la connexion</a></p>
</div>
</body>
</html>
