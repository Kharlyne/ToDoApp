package com.todoapp.handlers;

import com.todoapp.db.DatabaseClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;

public class ToDoHandler {

    private final SQLClient jdbc;

    public ToDoHandler() {
        this.jdbc = DatabaseClient.getClient();
    }
    public void createTodo(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject(); // Body auslesen
        String title = body.getString("title");
        String beschreibung = body.getString("beschreibung", ""); // Optionales Feld

        // Benutzer-ID aus Session holen
        Integer userId = ctx.session().get("userId");
        System.out.println("📌 [DEBUG] userId in session: " + userId);


        if (userId == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        if (title == null || title.isEmpty()) {
            ctx.response().setStatusCode(400).end("Titel erforderlich");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();
            String insertQuery = "INSERT INTO todos (created_by, title, beschreibung) VALUES (?, ?, ?)";

            connection.updateWithParams(insertQuery,
                    new io.vertx.core.json.JsonArray().add(userId).add(title).add(beschreibung),
                    res -> {
                        if (res.succeeded()) {
                            ctx.response().putHeader("content-type", "application/json")
                                    .end(new JsonObject().put("status", "ok").put("message", "ToDo erstellt!").encode());
                        } else {
                            ctx.response().setStatusCode(500).end("Fehler beim Erstellen des ToDos");
                        }
                        connection.close();
                    }
            );
        });
    }
    public void deleteTodo(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");
        if (userId == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        String idParam = ctx.pathParam("id");
        if (idParam == null) {
            ctx.response().setStatusCode(400).end("ID erforderlich");
            return;
        }

        int todoId;
        try {
            todoId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("Ungültige ID");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();
            // Correction : les bons noms de colonnes de ta base !
            String deleteQuery = "DELETE FROM todos WHERE todoId = ? AND created_by = ?";

            connection.updateWithParams(deleteQuery,
                    new io.vertx.core.json.JsonArray().add(todoId).add(userId),
                    res -> {
                        if (res.succeeded()) {
                            if (res.result().getUpdated() > 0) { // Vérifier qu'on a bien supprimé quelque chose
                                ctx.response().putHeader("content-type", "application/json")
                                        .end(new JsonObject().put("status", "ok").put("message", "ToDo gelöscht!").encode());
                            } else {
                                ctx.response().setStatusCode(404)
                                        .end(new JsonObject().put("status", "error").put("message", "ToDo nicht gefunden oder keine Berechtigung!").encode());
                            }
                        } else {
                            ctx.response().setStatusCode(500).end("Fehler beim Löschen der ToDo");
                        }
                        connection.close();
                    }
            );
        });
    }

    public void updateTodo(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId"); // 🟢 Cohérence avec create/delete
        if (userId == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        String idParam = ctx.pathParam("id");
        if (idParam == null) {
            ctx.response().setStatusCode(400).end("ID erforderlich");
            return;
        }

        int todoId;
        try {
            todoId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("Ungültige ID");
            return;
        }

        JsonObject body = ctx.body().asJsonObject();
        String title = body.getString("title");
        String beschreibung = body.getString("beschreibung"); // 👈 On garde "description" côté client

        if (title == null || title.isEmpty()) {
            ctx.response().setStatusCode(400).end("Titel erforderlich");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();
            String updateQuery = "UPDATE todos SET title = ?, beschreibung = ? WHERE todoId = ? AND created_by = ?";

            connection.updateWithParams(updateQuery,
                    new io.vertx.core.json.JsonArray().add(title).add(beschreibung).add(todoId).add(userId),
                    res -> {
                        if (res.succeeded()) {
                            if (res.result().getUpdated() > 0) {
                                ctx.response().putHeader("content-type", "application/json")
                                        .end(new JsonObject().put("status", "ok").put("message", "ToDo aktualisiert!").encode());
                            } else {
                                ctx.response().setStatusCode(404)
                                        .end(new JsonObject().put("status", "error").put("message", "ToDo nicht gefunden oder keine Berechtigung!").encode());
                            }
                        } else {
                            ctx.response().setStatusCode(500).end("Fehler beim Aktualisieren der ToDo");
                        }
                        connection.close();
                    }
            );
        });
    }
    public void getTodos(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");

        if (userId == null) {
            ctx.response().setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Nicht eingeloggt").encode());
            return;
        }

        jdbc.getConnection(connRes -> {
            if (connRes.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var conn = connRes.result();
            String query = "SELECT todoId, title, beschreibung, done FROM todos WHERE created_by = ?";

            conn.queryWithParams(query, new JsonArray().add(userId), res -> {
                if (res.succeeded()) {
                    var rows = res.result().getRows();
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("todos", rows).encode());
                } else {
                    ctx.response().setStatusCode(500).end("Fehler beim Laden der ToDos");
                }
                conn.close();
            });
        });
    }
    public void todoDoneStatus(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");

        if (userId == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        String idParam = ctx.pathParam("id");
        int todoId;
        try {
            todoId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("Ungültige ID");
            return;
        }

        JsonObject body = ctx.body().asJsonObject();
        boolean done = body.getBoolean("done", false); // false par défaut

        String updateQuery = "UPDATE todos SET done = ? WHERE todoId = ? AND created_by = ?";

        jdbc.getConnection(connRes -> {
            if (connRes.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var conn = connRes.result();

            conn.updateWithParams(
                    updateQuery,
                    new JsonArray().add(done ? 1 : 0).add(todoId).add(userId),
                    res -> {
                        if (res.succeeded()) {
                            ctx.response().putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("status", "ok").put("message", "ToDo aktualisiert!").encode());
                        } else {
                            ctx.response().setStatusCode(500).end("Fehler beim Aktualisieren");
                        }
                        conn.close();
                    }
            );
        });
    }




    public void getSharedTodos(RoutingContext ctx) {
        Integer userID = ctx.session().get("userID");
        if (userID == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();
            String selectQuery = "SELECT * FROM todos WHERE shared = true AND userID != ?";

            connection.queryWithParams(selectQuery, new io.vertx.core.json.JsonArray().add(userID), res -> {
                if (res.succeeded()) {
                    ctx.response().putHeader("content-type", "application/json")
                            .end(new JsonObject().put("status", "ok").put("todos", res.result().getRows()).encode());
                } else {
                    ctx.response().setStatusCode(500).end("Fehler beim Abrufen der ToDos");
                }
                connection.close();
            });
        });

    }
    public void shareTodo(RoutingContext ctx) {
        Integer ownerID = ctx.session().get("userId");
        if (ownerID == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        String todoIdParam = ctx.pathParam("id");
        if (todoIdParam == null) {
            ctx.response().setStatusCode(400).end("ID erforderlich");
            return;
        }

        int todoId;
        try {
            todoId = Integer.parseInt(todoIdParam);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("Ungültige ID");
            return;
        }

        JsonObject body = ctx.body().asJsonObject();
        String targetUsername = body.getString("username");
        if (targetUsername == null) {
            ctx.response().setStatusCode(400).end("Username erforderlich");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();

            // Erst prüfen, ob das ToDo wirklich dem Benutzer gehört
            String checkTodo = "SELECT id FROM todos WHERE id = ? AND userId = ?";
            connection.queryWithParams(checkTodo, new io.vertx.core.json.JsonArray().add(todoId).add(ownerID), todoRes -> {
                if (todoRes.succeeded() && todoRes.result().getNumRows() > 0) {
                    // Benutzer-ID vom Username holen
                    String getUser = "SELECT userID FROM users WHERE username = ?";
                    connection.queryWithParams(getUser, new io.vertx.core.json.JsonArray().add(targetUsername), userRes -> {
                        if (userRes.succeeded() && userRes.result().getNumRows() > 0) {
                            int targetUserID = userRes.result().getRows().get(0).getInteger("userID");

                            // Teilen in todo_shared_users einfügen
                            String insertShare = "INSERT INTO todo_shared_users (todo_id, user_id) VALUES (?, ?)";
                            connection.updateWithParams(insertShare, new io.vertx.core.json.JsonArray().add(todoId).add(targetUserID), shareRes -> {
                                if (shareRes.succeeded()) {
                                    ctx.response().putHeader("content-type", "application/json")
                                            .end(new JsonObject().put("status", "ok").put("message", "Todo geteilt!").encode());
                                } else {
                                    ctx.response().setStatusCode(500).end("Fehler beim Teilen der ToDo");
                                }
                                connection.close();
                            });

                        } else {
                            ctx.response().setStatusCode(404).end("Benutzer nicht gefunden");
                            connection.close();
                        }
                    });
                } else {
                    ctx.response().setStatusCode(403).end("Keine Berechtigung, dieses ToDo zu teilen");
                    connection.close();
                }
            });
        });
    }
    public void updateDoneStatus(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");
        if (userId == null) {
            ctx.response().setStatusCode(401).end("Nicht eingeloggt");
            return;
        }

        String idParam = ctx.pathParam("id");
        if (idParam == null) {
            ctx.response().setStatusCode(400).end("ID erforderlich");
            return;
        }

        int todoId;
        try {
            todoId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("Ungültige ID");
            return;
        }

        JsonObject body = ctx.body().asJsonObject();
        Boolean done = body.getBoolean("done");
        if (done == null) {
            ctx.response().setStatusCode(400).end("Feld 'done' erforderlich");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankfehler");
                return;
            }

            var connection = dbConn.result();
            String updateQuery = "UPDATE todos SET done = ? WHERE todoId = ? AND created_by = ?";

            connection.updateWithParams(updateQuery,
                    new io.vertx.core.json.JsonArray().add(done).add(todoId).add(userId),
                    res -> {
                        if (res.succeeded() && res.result().getUpdated() > 0) {
                            ctx.response().putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("status", "ok").put("message", "Aufgabe aktualisiert").encode());
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(new JsonObject().put("status", "error").put("message", "Nicht gefunden oder keine Berechtigung").encode());
                        }
                        connection.close();
                    });
        });
    }


}



