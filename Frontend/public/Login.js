document.getElementById("loginForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    if (username && password) {
        fetch("http://localhost:8888/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include", // ✅ important pour conserver la session
            body: JSON.stringify({ username: username, password: password })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Login fehlgeschlagen");
                }
                return response.json();
            })
            .then(data => {
                console.log("Login erfolgreich:", data);
                // Redirection après succès
                window.location.href = "Aufgaben.html";
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

    fetch("http://localhost:8888/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username, password })
    })
        .then(response => {
            if (!response.ok) throw new Error("Fehler bei der Registrierung");
            return response.json();
        })
        .then(data => {
            document.getElementById("registerResult").textContent = "✅ Registrierung erfolgreich!";
        })
        .catch(err => {
            console.error(err);
            document.getElementById("registerResult").textContent = "❌ Fehler bei der Registrierung.";
        });
});

