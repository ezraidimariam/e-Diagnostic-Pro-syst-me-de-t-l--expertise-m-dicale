<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Connexion</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f3f6fb; margin: 0; padding: 40px; }
        .box { max-width: 420px; margin: 60px auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
        h2 { text-align: center; margin-bottom: 25px; }
        form { display: flex; flex-direction: column; gap: 16px; }
        input { padding: 12px; border: 1px solid #d1d9e6; border-radius: 6px; }
        button { padding: 12px; border: none; border-radius: 6px; background: #2d6cdf; color: white; cursor: pointer; }
        .error { color: #b42318; background: #fef3f2; border: 1px solid #fecdca; border-radius: 6px; padding: 10px; }
    </style>
</head>
<body>
<div class="box">
    <h2>Connexion</h2>

    <% if (request.getAttribute("erreur") != null) { %>
        <div class="error"><%= request.getAttribute("erreur") %></div>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <input type="text" name="username" placeholder="Nom d'utilisateur" required>
        <input type="password" name="password" placeholder="Mot de passe" required>
        <button type="submit">Se connecter</button>
    </form>
</div>
</body>
</html>
