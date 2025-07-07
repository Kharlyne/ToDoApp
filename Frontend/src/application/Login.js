import { login, register } from "../data/authApi.js";

document.getElementById("loginForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    if (username && password) {
        login(username, password)
            .then(data => {
                console.log("Login erfolgreich:", data);
                window.location.href = "src/presentation/pages/Aufgaben.html"; // angepasst
            })
            .catch(error => {
                console.error("Login-Fehler:", error);
                document.getElementById("result").textContent = "Login fehlgeschlagen.";
            });
    } else {
        document.getElementById("result").textContent = "Benutzername und Passwort erforderlich.";
    }
});

document.getElementById("registerForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const username = document.getElementById("regUsername").value;
    const password = document.getElementById("regPassword").value;

    register(username, password)
        .then(() => {
            document.getElementById("registerResult").textContent = "✅ Registrierung erfolgreich!";
        })
        .catch(err => {
            console.error(err);
            document.getElementById("registerResult").textContent = "❌ Fehler bei der Registrierung.";
        });
});
