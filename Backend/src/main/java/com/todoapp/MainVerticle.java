package com.todoapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;


import com.todoapp.db.DatabaseClient;
import com.todoapp.handlers.AuthHandler;
import com.todoapp.handlers.ToDoHandler;
import com.todoapp.routes.AuthRoutes;
import com.todoapp.routes.ToDoRoutes;




public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        // Initialiser DB
        DatabaseClient.init(vertx);

        Router router = Router.router(vertx);

        // Middlewares
        router.route().handler(CorsHandler.create("http://localhost")
                .allowCredentials(true)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization"));

        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // Handlers
        AuthHandler authHandler = new AuthHandler();
        ToDoHandler todoHandler = new ToDoHandler();

        // Routes
        AuthRoutes.register(router, authHandler);
        ToDoRoutes.register(router, todoHandler);

        router.get("/").handler(ctx -> {
            ctx.response().putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("status", "läuft").encode());
        });

        // Server starten
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        System.out.println("✅ HTTP-Server läuft auf Port 8888!");
                        startPromise.complete();
                    } else {
                        System.out.println("❌ Fehler: " + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}

