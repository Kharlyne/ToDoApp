package com.todoapp.handlers;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.todoapp.services.AuthService;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public class AuthHandler {

    private final AuthService authService;

    private static final Logger logger = Logger.getLogger(AuthHandler.class.getName());

static {
    try {
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
        FileHandler fileHandler = new FileHandler("logs/auth.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(true);
    } catch (Exception e) {
        System.err.println("Logger setup failed in AuthHandler: " + e.getMessage());
    }
}


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
                        logger.info("📝 Registration attempt for user: " + username);

            } else {
                ctx.response().setStatusCode(400)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
                        logger.warning("❌ Registration failed: " + res.cause().getMessage());

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
                        logger.info("🔐 Login attempt for user: " + username);

            } else {
                ctx.response().setStatusCode(401)
                        .end(new JsonObject().put("status", "error").put("message", res.cause().getMessage()).encode());
                       logger.warning("❌ Login failed for user: " + username + " - " + res.cause().getMessage());

            }
        });
    }

    public void logout(RoutingContext ctx) {
        if (ctx.session() != null && !ctx.session().isDestroyed()) {
            ctx.session().destroy();
        }

        ctx.response().putHeader("content-type", "application/json")
                .end(new JsonObject().put("status", "ok").put("message", "Logout erfolgreich!").encode());
                logger.info("🔓 User logged out.");

    }
    public void checkAuth(RoutingContext ctx) {
        if (ctx.session() != null && ctx.session().get("userId") != null) {
            ctx.next(); // allow request to continue
            logger.fine("🔍 Checking session for authentication.");

        } else {
            logger.warning("⚠️ Unauthorized access attempt.");

            ctx.response().setStatusCode(401)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("status", "error").put("message", "Nicht eingeloggt").encode());
                    
        }
    }

}
