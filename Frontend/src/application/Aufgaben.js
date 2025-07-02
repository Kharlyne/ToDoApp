
import { getTodos, addTodo, deleteTodoById, toggleTodoDone } from "../data/todosApi.js"

let tasks = [];

document.addEventListener("DOMContentLoaded", () => {
    getTodos()
        .then(data => {
            tasks = data.todos || data;
            updateTaskList();
        })
        .catch(err => {
            console.error("Fehler beim Laden:", err);
        });

    document.getElementById('taskForm').addEventListener('submit', function (event) {
        event.preventDefault();
        const title = document.getElementById('taskTitle').value.trim();
        const description = document.getElementById('taskDetails').value.trim();

        if (title !== "") {
            addTodo({ title, description })
                .then(() => getTodos())
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

    document.getElementById('logoutBtn').addEventListener('click', () => {
        fetch("http://localhost:8888/logout", {
            method: "POST",
            credentials: "include"
        }).then(() => {
            window.location.href = "index.html";
        });
    });

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
            toggleTodoDone(task.todoId, this.checked)
                .catch(err => console.error("Fehler beim Aktualisieren:", err));
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
            <i class="fas fa-trash"></i> Löschen
        </button>
        <button class="btn btn-sm btn-outline-primary btn-block" onclick="shareTask(${index})">
            <i class="fas fa-share"></i> Teilen
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
    deleteTodoById(task.todoId)
        .then(() => {
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
    const details = task.beschreibung || task.description || task.details || '';
    document.getElementById('taskModalLabel').textContent = task.title;
    document.getElementById('modalTaskDetails').value = details;
    $('#taskModal').modal('show');
}
