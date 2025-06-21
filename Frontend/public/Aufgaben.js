let tasks = []; // Liste des tâches

// Charger les tâches au démarrage
document.addEventListener("DOMContentLoaded", () => {
    fetch("http://localhost:8888/todos", {
        method: "GET",
        credentials: "include"
    })
        .then(res => {
            if (!res.ok) throw new Error("Nicht eingeloggt");
            return res.json();
        })
        .then(data => {
            tasks = data.todos || data;
            updateTaskList();
        })
        .catch(err => {
            console.error("Fehler beim Laden:", err);
            // window.location.href = "index.html";
        });

    // Ajout d’une tâche
    document.getElementById('taskForm').addEventListener('submit', function (event) {
        event.preventDefault();
        const title = document.getElementById('taskTitle').value.trim();
        const description = document.getElementById('taskDetails').value.trim();

        if (title !== "") {
            fetch('http://localhost:8888/todos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ title, description })
            })
                .then(res => res.json())
                .then(() => {
                    // Recharger les tâches après ajout
                    return fetch("http://localhost:8888/todos", {
                        method: "GET",
                        credentials: "include"
                    });
                })
                .then(res => res.json())
                .then(data => {
                    tasks = data.todos || data;
                    updateTaskList();
                    document.getElementById('taskForm').reset();
                })
                .catch(err => {
                    console.error("Fehler beim Erstellen:", err);
                });
        }
    });

    // Déconnexion
    document.getElementById('logoutBtn').addEventListener('click', () => {
        fetch("http://localhost:8888/logout", {
            method: "POST",
            credentials: "include"
        }).then(() => {
            window.location.href = "index.html";
        });
    });

    // Afficher la modale Profil
    document.getElementById('profileBtn').addEventListener('click', () => {
        $('#profileModal').modal('show');
    });
});

function updateTaskList() {
    const taskList = document.getElementById('taskList');
    taskList.innerHTML = "";

    tasks.forEach((task, index) => {
        const li = document.createElement('li');
        li.className = "list-group-item d-flex justify-content-between align-items-center";

        const leftGroup = document.createElement('div');
        leftGroup.className = "d-flex align-items-center";

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.className = 'form-check-input me-2';
        checkbox.checked = task.done === 1 || task.done === true;
        checkbox.addEventListener('change', function () {
            fetch(`http://localhost:8888/todos/${task.todoId}/done`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ done: this.checked })
            }).catch(err => console.error("Fehler beim Aktualisieren:", err));
        });

        const taskTitle = document.createElement('span');
        taskTitle.textContent = task.title;
        taskTitle.style.cursor = 'pointer';
        taskTitle.setAttribute('data-index', index);
        taskTitle.addEventListener('click', openTaskModal);

        leftGroup.appendChild(checkbox);
        leftGroup.appendChild(taskTitle);

        const menuButton = document.createElement('button');
        menuButton.className = "btn btn-link text-dark p-0 m-0";
        menuButton.innerHTML = '<i class="fas fa-ellipsis-v"></i>';
        menuButton.setAttribute('data-index', index);
        menuButton.addEventListener('click', function (e) {
            e.stopPropagation();
            openTaskMenu(index, li);
        });

        li.appendChild(leftGroup);
        li.appendChild(menuButton);
        taskList.appendChild(li);
    });
}

function openTaskMenu(index, parentElement) {
    const oldMenu = document.getElementById('taskMenu');
    if (oldMenu) oldMenu.remove();

    const menu = document.createElement('div');
    menu.id = 'taskMenu';
    menu.className = 'card position-absolute p-2';
    menu.style.top = '50px';
    menu.style.right = '10px';
    menu.style.zIndex = '1000';
    menu.style.width = '120px';
    menu.innerHTML = `
        <button class="btn btn-sm btn-outline-danger btn-block mb-2" onclick="deleteTask(${index})">
            <i class="fas fa-trash"></i> Supprimer
        </button>
        <button class="btn btn-sm btn-outline-primary btn-block" onclick="shareTask(${index})">
            <i class="fas fa-share"></i> Partager
        </button>
    `;

    parentElement.appendChild(menu);

    document.addEventListener('click', function closeMenu(event) {
        if (!menu.contains(event.target)) {
            menu.remove();
            document.removeEventListener('click', closeMenu);
        }
    });
}

function deleteTask(index) {
    const task = tasks[index];
    const id = task.todoId; // 👈 attention à l'exact nom de propriété renvoyée par ton backend

    fetch(`http://localhost:8888/todos/${id}`, {
        method: "DELETE",
        credentials: "include"
    })
        .then(res => {
            if (!res.ok) throw new Error("❌ La suppression a échoué.");
            // Si succès, retirer du tableau local et mettre à jour l'affichage
            tasks.splice(index, 1);
            updateTaskList();
        })
        .catch(err => {
            console.error("❌ Fehler beim Löschen:", err);
            alert("La suppression a échoué. Voir console.");
        });
}

function shareTask(index) {
    const task = tasks[index];
    navigator.clipboard.writeText(`Tâche: ${task.title}\nDétails: ${task.description}`);
    alert('Tâche copiée dans le presse-papiers !');
}
function openTaskModal(event) {
    const index = event.target.getAttribute('data-index');
    const task = tasks[index];

    // Debug
    console.log("Données reçues:", task);

    // Trouve le champ description peu importe son nom
    const details = task.beschreibung || task.description || task.details || task.beschreibung || '';

    document.getElementById('taskModalLabel').textContent = task.title;
    document.getElementById('modalTaskDetails').value = details;
    $('#taskModal').modal('show');
}


