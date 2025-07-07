package com.todoapp.repositories;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

public class AuthRepository {

    private final SQLClient client;

    public AuthRepository(SQLClient client) {
        this.client = client;
    }

    public Future<Boolean> isUsernameTaken(String username) {
        Promise<Boolean> promise = Promise.promise();
        String query = "SELECT * FROM users WHERE username = ?";

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }

            SQLConnection connection = conn.result();
            connection.queryWithParams(query, new JsonArray().add(username), res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete(res.result().getNumRows() > 0);
                } else {
                    promise.fail(res.cause());
                }
            });
        });

        return promise.future();
    }

    public Future<Void> insertUser(String username, String hashedPassword) {
        Promise<Void> promise = Promise.promise();
        String insert = "INSERT INTO users (username, password) VALUES (?, ?)";

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }

            SQLConnection connection = conn.result();
            connection.updateWithParams(insert, new JsonArray().add(username).add(hashedPassword), res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(res.cause());
                }
            });
        });

        return promise.future();
    }

    public Future<JsonObject> getUserByUsername(String username) {
        Promise<JsonObject> promise = Promise.promise();
        String query = "SELECT * FROM users WHERE username = ?";

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }

            SQLConnection connection = conn.result();
            connection.queryWithParams(query, new JsonArray().add(username), res -> {
                connection.close();
                if (res.succeeded() && res.result().getNumRows() > 0) {
                    promise.complete(res.result().getRows().get(0));
                } else {
                    promise.fail("Benutzer nicht gefunden");
                }
            });
        });

        return promise.future();
    }
}
