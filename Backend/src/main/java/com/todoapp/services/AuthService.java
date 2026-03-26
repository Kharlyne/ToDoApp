package com.todoapp.services;

import com.todoapp.repositories.AuthRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class AuthService {

    private final AuthRepository repository;

    public AuthService(AuthRepository repository) {
        this.repository = repository;
    }

    public Future<JsonObject> register(String username, String password) {
        Promise<JsonObject> promise = Promise.promise();

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            promise.fail("Benutzername und Passwort erforderlich");
            return promise.future();
        }

        repository.isUsernameTaken(username).onComplete(res -> {
            if (res.failed()) {
                promise.fail(res.cause());
            } else if (res.result()) {
                promise.fail("Benutzername bereits vergeben");
            } else {
                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                repository.insertUser(username, hashedPassword).onComplete(insert -> {
                    if (insert.succeeded()) {
                        repository.getUserByUsername(username).onComplete(fetch -> {
                            if (fetch.succeeded()) {
                                JsonObject user = fetch.result();
                                promise.complete(user);
                            } else {
                                promise.fail("Fehler beim Abrufen der Benutzer-ID");
                            }
                        });
                    } else {
                        promise.fail("Fehler beim Einfügen des Benutzers");
                    }
                });
            }
        });

        return promise.future();
    }

    public Future<JsonObject> login(String username, String password) {
        Promise<JsonObject> promise = Promise.promise();

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            promise.fail("Benutzername und Passwort erforderlich");
            return promise.future();
        }

        repository.getUserByUsername(username).onComplete(res -> {
            if (res.failed()) {
                promise.fail("Benutzer nicht gefunden");
            } else {
                JsonObject user = res.result();
                String hashedPassword = user.getString("password");
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);

                if (result.verified) {
                    promise.complete(user);
                } else {
                    promise.fail("Falsches Passwort");
                }
            }
        });

        return promise.future();
    }

    public Future<String> logout() {
        return Future.succeededFuture("Logout erfolgreich");
    }
}
