package com.todoapp.handlers;

import com.todoapp.services.AuthService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AuthHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    public void register(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");

        authService.register(username, password).onComplete(res -> {
            if (res.succeeded()) {
                JsonObject user = res.result();
                ctx.session().put("userId", user.getInteger("userId"));
                ctx.session().put("username", user.getString("username"));

                ctx.response().putHeader("content-type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", "Registrierung erfolgreich!").encode());
            } else {
                ctx.response().setStatusCode(400)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void login(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");

        authService.login(username, password).onComplete(res -> {
            if (res.succeeded()) {
                JsonObject user = res.result();
                ctx.session().put("userId", user.getInteger("userId"));
                ctx.session().put("username", user.getString("username"));

                ctx.response().putHeader("content-type", "application/json")
                        .end(new JsonObject().put("status", "ok").put("message", "Login erfolgreich").encode());
            } else {
                ctx.response().setStatusCode(401)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
            }
        });
    }

    public void logout(RoutingContext ctx) {
        if (ctx.session() != null && !ctx.session().isDestroyed()) {
            ctx.session().destroy();
        }

        ctx.response().putHeader("content-type", "application/json")
                .end(new JsonObject().put("status", "ok").put("message", "Logout erfolgreich!").encode());
    }
}
