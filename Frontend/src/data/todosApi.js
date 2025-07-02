// src/data/todosApi.js

const BASE_URL = "http://localhost:8888/todos";

export async function getTodos() {
    const res = await fetch(BASE_URL, { credentials: "include" });
    if (!res.ok) throw new Error("Nicht eingeloggt");
    const data = await res.json();
    return data.todos || data;
}

export async function addTodo(title, description) {
    const res = await fetch(BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ title, description })
    });
    if (!res.ok) throw new Error("Fehler beim Erstellen");
    return getTodos(); // direkt aktualisieren
}

export async function deleteTodoById() {
    const res = await fetch(`${BASE_URL}/${id}`, {
        method: "DELETE",
        credentials: "include"
    });
    if (!res.ok) throw new Error("Fehler beim Löschen");
}

export async function toggleTodoDone(id, done) {
    const res = await fetch(`${BASE_URL}/${id}/done`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ done })
    });
    if (!res.ok) throw new Error("Fehler beim Aktualisieren");
}
