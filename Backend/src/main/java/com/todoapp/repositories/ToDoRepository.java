package com.todoapp.repositories;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

public class ToDoRepository {

    private final SQLClient client;

    public ToDoRepository(SQLClient client) {
        this.client = client;
    }

    public Future<Void> insertTodo(int userId, String title, String beschreibung) {
        Promise<Void> promise = Promise.promise();
        String query = "INSERT INTO todos (created_by, title, beschreibung) VALUES (?, ?, ?)";
        JsonArray params = new JsonArray().add(userId).add(title).add(beschreibung);

        executeUpdate(query, params, promise);
        return promise.future();
    }

    public Future<Boolean> deleteTodo(int userId, int todoId) {
        Promise<Boolean> promise = Promise.promise();
        String query = "DELETE FROM todos WHERE todoId = ? AND created_by = ?";
        JsonArray params = new JsonArray().add(todoId).add(userId);

        executeUpdateCount(query, params, promise);
        return promise.future();
    }

    public Future<Boolean> updateTodo(int userId, int todoId, String title, String beschreibung) {
        Promise<Boolean> promise = Promise.promise();
        String query = "UPDATE todos SET title = ?, beschreibung = ? WHERE todoId = ? AND created_by = ?";
        JsonArray params = new JsonArray().add(title).add(beschreibung).add(todoId).add(userId);

        executeUpdateCount(query, params, promise);
        return promise.future();
    }

    public Future<List<JsonObject>> getTodosByUser(int userId) {
        Promise<List<JsonObject>> promise = Promise.promise();
        String query = "SELECT todoId, title, beschreibung, done FROM todos WHERE created_by = ?";
        JsonArray params = new JsonArray().add(userId);

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }
            SQLConnection connection = conn.result();
            connection.queryWithParams(query, params, res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete(res.result().getRows());
                } else {
                    promise.fail(res.cause());
                }
            });
        });

        return promise.future();
    }

    public Future<Boolean> updateDoneStatus(int userId, int todoId, boolean done) {
        Promise<Boolean> promise = Promise.promise();
        String query = "UPDATE todos SET done = ? WHERE todoId = ? AND created_by = ?";
        JsonArray params = new JsonArray().add(done ? 1 : 0).add(todoId).add(userId);

        executeUpdateCount(query, params, promise);
        return promise.future();
    }

    public Future<List<JsonObject>> getSharedTodosExcludingUser(int userId) {
        Promise<List<JsonObject>> promise = Promise.promise();
        String query = "SELECT * FROM todos WHERE shared = true AND userID != ?";
        JsonArray params = new JsonArray().add(userId);

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }
            SQLConnection connection = conn.result();
            connection.queryWithParams(query, params, res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete(res.result().getRows());
                } else {
                    promise.fail(res.cause());
                }
            });
        });

        return promise.future();
    }

    public Future<Boolean> shareTodo(int ownerId, int todoId, int targetUserId) {
        Promise<Boolean> promise = Promise.promise();
        String query = "INSERT INTO todo_shared_users (todo_id, user_id) VALUES (?, ?)";
        JsonArray params = new JsonArray().add(todoId).add(targetUserId);

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }

            SQLConnection connection = conn.result();
            connection.updateWithParams(query, params, res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete(true);
                } else {
                    promise.fail(res.cause());
                }
            });
        });

        return promise.future();
    }

    public Future<Boolean> ownsTodo(int userId, int todoId) {
        Promise<Boolean> promise = Promise.promise();
        String query = "SELECT todoId FROM todos WHERE todoId = ? AND created_by = ?";
        JsonArray params = new JsonArray().add(todoId).add(userId);

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }

            SQLConnection connection = conn.result();
            connection.queryWithParams(query, params, res -> {
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

    public Future<Integer> getUserIdByUsername(String username) {
        Promise<Integer> promise = Promise.promise();
        String query = "SELECT userID FROM users WHERE username = ?";
        JsonArray params = new JsonArray().add(username);

        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }

            SQLConnection connection = conn.result();
            connection.queryWithParams(query, params, res -> {
                connection.close();
                if (res.succeeded() && res.result().getNumRows() > 0) {
                    int id = res.result().getRows().get(0).getInteger("userID");
                    promise.complete(id);
                } else {
                    promise.fail("Benutzer nicht gefunden");
                }
            });
        });

        return promise.future();
    }

    // Utility methods

    private void executeUpdate(String query, JsonArray params, Promise<Void> promise) {
        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }
            SQLConnection connection = conn.result();
            connection.updateWithParams(query, params, res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(res.cause());
                }
            });
        });
    }

    private void executeUpdateCount(String query, JsonArray params, Promise<Boolean> promise) {
        client.getConnection(conn -> {
            if (conn.failed()) {
                promise.fail(conn.cause());
                return;
            }
            SQLConnection connection = conn.result();
            connection.updateWithParams(query, params, res -> {
                connection.close();
                if (res.succeeded()) {
                    promise.complete(res.result().getUpdated() > 0);
                } else {
                    promise.fail(res.cause());
                }
            });
        });
    }
}
