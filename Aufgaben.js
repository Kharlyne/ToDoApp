// dashboard.js
document.addEventListener('DOMContentLoaded', function() {
    // Logout Functionality
    document.getElementById('logoutBtn')?.addEventListener('click', function() {
        localStorage.removeItem('authToken'); // Supprimer le token si existant
        window.location.href = 'login.html';
    });

    // Task Completion Toggle
    document.querySelectorAll('.task-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', function(e) {
            const taskItem = this.closest('.task-item');
            taskItem.classList.toggle('completed');
            e.stopPropagation();

            // Sauvegarder l'état dans localStorage (à compléter)
            const taskId = taskItem.dataset.taskId;
            // ...logique de sauvegarde...
        });
    });

    // Task Click Redirection
    document.querySelectorAll('.task-content').forEach(task => {
        task.addEventListener('click', function() {
            const taskId = this.closest('.task-item').dataset.taskId;
            window.location.href = `task-detail.html?id=${taskId}`;
        });
    });

    // Modal Form Handling
    const addTaskForm = document.getElementById('addTaskForm');
    addTaskForm?.addEventListener('submit', function(e) {
        e.preventDefault();
        const title = document.getElementById('taskInput').value;
        const details = document.getElementById('taskDetails').value;

        // Ajouter la nouvelle tâche (simulé)
        addNewTask(title, details);

        // Fermer le modal
        bootstrap.Modal.getInstance(document.getElementById('taskModal')).hide();
        addTaskForm.reset();
    });
});

// Fonction pour ajouter une tâche (à adapter avec votre logique)
function addNewTask(title, details) {
    const taskList = document.getElementById('taskList');
    const newTask = document.createElement('li');
    newTask.className = 'list-group-item task-item d-flex align-items-center';
    newTask.dataset.taskId = Date.now(); // ID temporaire

    newTask.innerHTML = `
        <input type="checkbox" class="task-checkbox me-3 form-check-input">
        <div class="task-content">
            ${title}
            <div class="text-muted small">${details || 'Keine Details'}</div>
        </div>
        <div>
            <button class="btn btn-sm btn-outline-secondary me-2 edit-btn">
                <i class="bi bi-pencil-square"></i>
            </button>
            <button class="btn btn-sm btn-outline-danger delete-btn">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;

    taskList.appendChild(newTask);
    // Ajouter les écouteurs d'événements à la nouvelle tâche
    attachTaskEventListeners(newTask);
}

// Fonction helper pour attacher les événements
function attachTaskEventListeners(taskElement) {
    taskElement.querySelector('.task-checkbox').addEventListener('change', function(e) {
        taskElement.classList.toggle('completed');
        e.stopPropagation();
    });

    taskElement.querySelector('.task-content').addEventListener('click', function() {
        const taskId = taskElement.dataset.taskId;
        window.location.href = `task-detail.html?id=${taskId}`;
    });

    // Ajouter ici les listeners pour edit/delete si nécessaire
}