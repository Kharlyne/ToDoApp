document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.getElementById('loginForm');
    const resultDiv = document.getElementById('result');
    const registerBtn = document.getElementById('registerBtn');

    // Einloggen
    loginForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();

        if (!username || !password) {
            resultDiv.textContent = "Bitte Benutzername und Passwort eingeben!";
            return;
        }

        try {
            const response = await fetch("/api/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            const data = await response.json();

            if (data.status === "success") {
                resultDiv.textContent = "Login erfolgreich!";
                resultDiv.className = "text-success text-center mt-3";
                setTimeout(() => window.location.href = "Aufgaben.html", 1000);
            } else {
                resultDiv.textContent = "Login fehlgeschlagen!";
                resultDiv.className = "text-danger text-center mt-3";
            }
        } catch (error) {
            resultDiv.textContent = "Fehler beim Login: " + error.message;
        }
    });

    // Registrieren
    registerBtn.addEventListener('click', async function () {
        const username = document.getElementById('username').value.trim();l
        const password = document.getElementById('password').value.trim();

        if (!username || !password) {
            resultDiv.textContent = "Bitte Benutzername und Passwort eingeben!";
            return;
        }

        try {
            resultDiv.textContent = "Registrierung läuft...";

            const response = await fetch("/api/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            const data = await response.json();k

            if (data.status === "success") {
                resultDiv.textContent = "Registrierung erfolgreich!";
                resultDiv.className = "text-success text-center mt-3";
            } else {
                resultDiv.textContent = "Benutzername existiert bereits!";
                resultDiv.className = "text-danger text-center mt-3";
            }
        } catch (error) {
            resultDiv.textContent = "Fehler bei der Registrierung: " + error.message;
        }
    });
});
