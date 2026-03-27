# ToDoApp

ToDoApp ist eine einfache Webanwendung zur Verwaltung von Aufgaben.  
Das Projekt besteht aus einem Frontend in HTML/CSS/JavaScript, einem Backend in Java mit Vert.x und einer MariaDB-Datenbank.

## Start des Projekts

### Datenbank
MariaDB starten, dann die Datenbank `todo_app` anlegen und das SQL-Skript ausführen:

```sql
CREATE DATABASE todo_app;
USE todo_app;
SOURCE C:/Pfad/zum/Projekt/backend/init.sql;

### Backend starten
cd backend
mvn exec:java

Das Backend läuft dann unter:

http://localhost:8888

### Frontend starten
cd frontend
npm install
npm run dev

Danach die von Vite angezeigte URL im Browser öffnen.

###Start mit Docker

Mit Docker Compose:

docker compose up --build

Zum Stoppen:

docker compose down

Häufige Probleme
Registrierung funktioniert nicht: Datenbank läuft nicht, falscher Port oder falsche Zugangsdaten
Connections could not be acquired from the underlying database: keine Verbindung zur Datenbank
Address already in use: bind: Port ist bereits belegt
