package com.todoapp;

import com.todoapp.db.DatabaseClient;
import com.todoapp.handlers.AuthHandler;
import com.todoapp.handlers.ToDoHandler;
import com.todoapp.repositories.AuthRepository;
import com.todoapp.repositories.ToDoRepository;
import com.todoapp.routes.AuthRoutes;
import com.todoapp.routes.ToDoRoutes;
import com.todoapp.services.AuthService;
import com.todoapp.services.ToDoService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        // Init DB
        DatabaseClient.init(vertx);
        SQLClient sqlClient = DatabaseClient.getClient();

        // DI setup
        AuthRepository authRepo = new AuthRepository(sqlClient);
        ToDoRepository todoRepo = new ToDoRepository(sqlClient);

        AuthService authService = new AuthService(authRepo);
        ToDoService todoService = new ToDoService(todoRepo);

        AuthHandler authHandler = new AuthHandler(authService);
        ToDoHandler todoHandler = new ToDoHandler(todoService);

        // Router & middleware
        Router router = Router.router(vertx);

        router.route().handler(CorsHandler.create("http://localhost:5173")

                .allowCredentials(true)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization"));

        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // Route registration
        AuthRoutes.register(router, authHandler);
        ToDoRoutes.register(router, todoHandler);


        // Health check
        router.get("/").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("status", "running").encode());
        });

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        System.out.println("✅ Server läuft auf Port 8888");
                        startPromise.complete();
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
        // Debug-Middleware (nur temporär)
        router.route().handler(ctx -> {
            System.out.println("🧠 Session-Daten: " + ctx.session().data());
            ctx.next();
        });

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
