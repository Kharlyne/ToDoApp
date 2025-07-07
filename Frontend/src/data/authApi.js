const BASE_URL = import.meta.env.VITE_BACKEND_URL;

export async function login(username, password) {
    const res = await fetch(`${BASE_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username, password })
    });
    if (!res.ok) throw new Error("Login fehlgeschlagen");
    return res.json();
}

export async function register(username, password) {
    const res = await fetch(`${BASE_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username, password })
    });
    if (!res.ok) throw new Error("Registrierung fehlgeschlagen");
    return res.json();
}
