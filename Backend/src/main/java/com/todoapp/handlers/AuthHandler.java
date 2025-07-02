package com.todoapp.handlers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.todoapp.db.DatabaseClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonArray;

public class AuthHandler {

    private final SQLClient jdbc;

    public AuthHandler() {
        this.jdbc = DatabaseClient.getClient();
    }

    public void register(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");

        if (username == null || password == null) {
            ctx.response().setStatusCode(400).end("Benutzername und Passwort erforderlich");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();
            String checkQuery = "SELECT * FROM users WHERE username = ?";
            connection.queryWithParams(checkQuery, new JsonArray().add(username), checkRes -> {
                if (checkRes.succeeded() && checkRes.result().getNumRows() > 0) {
                    ctx.response().setStatusCode(409)
                            .end(new JsonObject().put("status", "error").put("message", "Benutzername bereits vergeben").encode());
                    connection.close();
                } else {
                    String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                    String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";

                    connection.updateWithParams(insertQuery, new JsonArray().add(username).add(hashedPassword), insertRes -> {
                        if (insertRes.succeeded()) {
                            String getIdQuery = "SELECT userId FROM users WHERE username = ?";
                            connection.queryWithParams(getIdQuery, new JsonArray().add(username), idRes -> {
                                if (idRes.succeeded()) {
                                    int userId = idRes.result().getRows().get(0).getInteger("userId");
                                    ctx.session().put("userId", userId);
                                    ctx.session().put("username", username);
                                    ctx.response().putHeader("content-type", "application/json")
                                            .end(new JsonObject().put("status", "ok").put("message", "Registrierung erfolgreich!").encode());
                                } else {
                                    ctx.response().setStatusCode(500).end("Fehler beim Abrufen der Benutzer-ID");
                                }
                                connection.close();
                            });
                        } else {
                            ctx.response().setStatusCode(500).end("Fehler beim Einfügen des Benutzers");
                            connection.close();
                        }
                    });
                }
            });
        });
    }

    public void login(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");

        if (username == null || password == null) {
            ctx.response().setStatusCode(400).end("Benutzername und Passwort erforderlich");
            return;
        }

        jdbc.getConnection(dbConn -> {
            if (dbConn.failed()) {
                ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
                return;
            }

            var connection = dbConn.result();
            String query = "SELECT * FROM users WHERE username = ?";
            connection.queryWithParams(query, new JsonArray().add(username), res -> {
                if (res.succeeded() && res.result().getNumRows() > 0) {
                    var row = res.result().getRows().get(0);
                    String hashedPassword = row.getString("password");
                    int userId = row.getInteger("userId");

                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
                    if (result.verified) {
                        ctx.session().put("userId", userId);
                        ctx.session().put("username", username);
                        ctx.response().putHeader("content-type", "application/json")
                                .end(new JsonObject().put("status", "ok").put("message", "Login erfolgreich").encode());
                    } else {
                        ctx.response().setStatusCode(401).end("Falsches Passwort");
                    }
                } else {
                    ctx.response().setStatusCode(404).end("Benutzer nicht gefunden");
                }
                connection.close();
            });
        });
    }

    public void logout(RoutingContext ctx) {
        if (ctx.session() != null && !ctx.session().isDestroyed()) {
            ctx.session().destroy();
            ctx.response().putHeader("content-type", "application/json")
                    .end(new JsonObject().put("status", "ok").put("message", "Logout erfolgreich!").encode());
        } else {
            ctx.response().putHeader("content-type", "application/json")
                    .end(new JsonObject().put("status", "ok").put("message", "Keine aktive Session!").encode());
        }
    }
}
