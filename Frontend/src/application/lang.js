const translations = {
    de: {
        login_title: "ToDo App Login",
        username_label: "Benutzername:",
        password_label: "Passwort:",
        login_button: "Einloggen",
        reg_username: "Neuer Benutzername:",
        reg_password: "Neues Passwort:",
        register_button: "Registrieren",
        name_label: "Name:",
        email_label: "Email:",
        close: "Schließen",
        my_tasks: "Meine Aufgaben",
        task_input: "Fügen Sie eine Aufgabe ein",
        task_description: "fügen Sie eine Beschreibung ein",
        task_description_title: "Beschreibung der Aufgabe",
        logout: "Ausloggen",
        profile: "Profil"
    },
    en: {
        login_title: "ToDo App Login",
        username_label: "Username:",
        password_label: "Password:",
        login_button: "Login",
        reg_username: "New Username:",
        reg_password: "New Password:",
        register_button: "Register",
        name_label: "Name:",
        email_label: "Email:",
        close: "Close",
        my_tasks: "My Tasks",
        task_input: "Enter a task",
        task_description: "Enter a description",
        task_description_title: "Task Description",
        logout: "Logout",
        profile: "Profile"
    }
};

document.addEventListener("DOMContentLoaded", () => {
    const selector = document.getElementById("lang-select");
    if (selector) {
        selector.addEventListener("change", (e) => {
            const lang = e.target.value;
            updateTexts(lang);
            localStorage.setItem("lang", lang);
        });
        const savedLang = localStorage.getItem("lang") || "de";
        selector.value = savedLang;
        updateTexts(savedLang);
    }
});

function updateTexts(lang) {
    document.querySelectorAll("[data-i18n]").forEach((el) => {
        const key = el.getAttribute("data-i18n");
        if (translations[lang][key]) {
            el.textContent = translations[lang][key];
        }
    });
    document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
        const key = el.getAttribute("data-i18n-placeholder");
        if (translations[lang][key]) {
            el.placeholder = translations[lang][key];
        }
    });
}
