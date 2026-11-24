<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Personal Library</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="welcome-page">
    <header class="welcome-header">
        <nav class="welcome-nav">
            <a href="${pageContext.request.contextPath}/login"
               class="welcome-button welcome-button-outline">Log in</a>
            <a href="${pageContext.request.contextPath}/register"
               class="welcome-button welcome-button-primary">Register</a>
        </nav>
    </header>

    <section class="welcome-hero">
        <div class="welcome-content">
            <h1>Welcome To<br><span>My Library</span></h1>
            <p class="welcome-description">
                A personal reading tracker — keep track of the books you have read,
                manage your reading goals, write private notes, and log your reading sessions.
            </p>
            <div class="welcome-actions">
                <a href="${pageContext.request.contextPath}/register"
                   class="welcome-button welcome-button-primary">Get Started</a>
                <a href="${pageContext.request.contextPath}/login"
                   class="welcome-text-link">Already have an account?</a>
            </div>
        </div>
        <div class="welcome-image-wrapper">
            <img src="${pageContext.request.contextPath}/images/welcome_buku.jpg"
                 alt="A collection of hardcover books" class="welcome-image">
        </div>
    </section>
</main>
</body>
</html>
