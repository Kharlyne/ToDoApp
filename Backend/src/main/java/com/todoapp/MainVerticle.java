package com.todoapp;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
private static final Logger logger = Logger.getLogger(MainVerticle.class.getName());

    @Override
    public void start(Promise<Void> startPromise) {

        try {
    // Crée le dossier logs s’il n’existe pas
    java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));

    FileHandler fh = new FileHandler("logs/server.log", true); // true = append
    fh.setFormatter(new SimpleFormatter());
    logger.addHandler(fh);
    logger.setLevel(Level.ALL);
    logger.setUseParentHandlers(true); // utile pour voir aussi les logs dans Docker ou terminal
} catch (Exception e) {
    e.printStackTrace();
}



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
                        logger.info("✅ Backend started.");
                        startPromise.complete();
                    } else {
                        logger.severe("❌ Fehler beim Starten des Servers: " + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });
        // Debug-Middleware (nur temporär)
        router.route().handler(ctx -> {
            logger.fine("🧠 Session-Daten: " + ctx.session().data());
            System.out.println("🧠 Session-Daten: " + ctx.session().data());
            ctx.next();
        });

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
