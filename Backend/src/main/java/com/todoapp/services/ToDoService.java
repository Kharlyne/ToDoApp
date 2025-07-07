package com.todoapp.services;

import com.todoapp.repositories.ToDoRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ToDoService {

    private final ToDoRepository repository;

    public ToDoService(ToDoRepository repository) {
        this.repository = repository;
    }

    public Future<String> createTodo(Integer userId, String title, String beschreibung) {
        Promise<String> promise = Promise.promise();

        if (userId == null) {
            promise.fail("Nicht eingeloggt");
        } else if (title == null || title.isBlank()) {
            promise.fail("Titel erforderlich");
        } else {
            repository.insertTodo(userId, title, beschreibung)
                    .onSuccess(r -> promise.complete("ToDo erstellt!"))
                    .onFailure(promise::fail);
        }

        return promise.future();
    }

    public Future<String> deleteTodo(Integer userId, int todoId) {
        Promise<String> promise = Promise.promise();

        if (userId == null) {
            promise.fail("Nicht eingeloggt");
        } else {
            repository.deleteTodo(userId, todoId).onComplete(res -> {
                if (res.succeeded() && res.result()) {
                    promise.complete("ToDo gelöscht!");
                } else {
                    promise.fail("ToDo nicht gefunden oder keine Berechtigung!");
                }
            });
        }

        return promise.future();
    }

    public Future<String> updateTodo(Integer userId, int todoId, String title, String beschreibung) {
        Promise<String> promise = Promise.promise();

        if (userId == null) {
            promise.fail("Nicht eingeloggt");
        } else if (title == null || title.isBlank()) {
            promise.fail("Titel erforderlich");
        } else {
            repository.updateTodo(userId, todoId, title, beschreibung).onComplete(res -> {
                if (res.succeeded() && res.result()) {
                    promise.complete("ToDo aktualisiert!");
                } else {
                    promise.fail("ToDo nicht gefunden oder keine Berechtigung!");
                }
            });
        }

        return promise.future();
    }

    public Future<List<JsonObject>> getTodos(Integer userId) {
        if (userId == null) return Future.failedFuture("Nicht eingeloggt");
        return repository.getTodosByUser(userId);
    }

    public Future<String> updateDoneStatus(Integer userId, int todoId, boolean done) {
        Promise<String> promise = Promise.promise();

        if (userId == null) {
            promise.fail("Nicht eingeloggt");
        } else {
            repository.updateDoneStatus(userId, todoId, done).onComplete(res -> {
                if (res.succeeded() && res.result()) {
                    promise.complete("Aufgabe aktualisiert");
                } else {
                    promise.fail("Nicht gefunden oder keine Berechtigung");
                }
            });
        }

        return promise.future();
    }

    public Future<List<JsonObject>> getSharedTodos(Integer userId) {
        if (userId == null) return Future.failedFuture("Nicht eingeloggt");
        return repository.getSharedTodosExcludingUser(userId);
    }

    public Future<String> shareTodo(Integer ownerId, int todoId, String targetUsername) {
        Promise<String> promise = Promise.promise();

        if (ownerId == null) {
            promise.fail("Nicht eingeloggt");
            return promise.future();
        }

        repository.ownsTodo(ownerId, todoId).onComplete(check -> {
            if (check.failed() || !check.result()) {
                promise.fail("Keine Berechtigung, dieses ToDo zu teilen");
            } else {
                repository.getUserIdByUsername(targetUsername).onComplete(userRes -> {
                    if (userRes.failed()) {
                        promise.fail("Benutzer nicht gefunden");
                    } else {
                        int targetUserId = userRes.result();
                        repository.shareTodo(ownerId, todoId, targetUserId).onComplete(shareRes -> {
                            if (shareRes.succeeded()) {
                                promise.complete("Todo geteilt!");
                            } else {
                                promise.fail("Fehler beim Teilen der ToDo");
                            }
                        });
                    }
                });
            }
        });

        return promise.future();
    }
}
