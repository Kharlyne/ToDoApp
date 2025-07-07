package com.todoapp.handlers;

import com.todoapp.services.ToDoService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ToDoHandler {

    private final ToDoService service;

    public ToDoHandler(ToDoService service) {
        this.service = service;
    }

    public void createTodo(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String title = body.getString("title");
        String beschreibung = body.getString("beschreibung", "");
        Integer userId = ctx.session().get("userId");

        service.createTodo(userId, title, beschreibung).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", res.result()).encode());
            } else {
                ctx.response().setStatusCode(400)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void deleteTodo(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");
        int todoId = Integer.parseInt(ctx.pathParam("id"));

        service.deleteTodo(userId, todoId).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", res.result()).encode());
            } else {
                ctx.response().setStatusCode(404)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void updateTodo(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");
        int todoId = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        String title = body.getString("title");
        String beschreibung = body.getString("beschreibung", "");

        service.updateTodo(userId, todoId, title, beschreibung).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", res.result()).encode());
            } else {
                ctx.response().setStatusCode(404)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void getTodos(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");

        service.getTodos(userId).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("todos", res.result()).encode());
            } else {
                ctx.response().setStatusCode(401)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void updateDoneStatus(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");
        int todoId = Integer.parseInt(ctx.pathParam("id"));
        boolean done = ctx.body().asJsonObject().getBoolean("done", false);

        service.updateDoneStatus(userId, todoId, done).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", res.result()).encode());
            } else {
                ctx.response().setStatusCode(404)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void getSharedTodos(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");

        service.getSharedTodos(userId).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("todos", res.result()).encode());
            } else {
                ctx.response().setStatusCode(401)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void shareTodo(RoutingContext ctx) {
        Integer userId = ctx.session().get("userId");
        int todoId = Integer.parseInt(ctx.pathParam("id"));
        String targetUsername = ctx.body().asJsonObject().getString("username");

        service.shareTodo(userId, todoId, targetUsername).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", res.result()).encode());
            } else {
                ctx.response().setStatusCode(400)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }
}
