package com.todoapp.db;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

public class DatabaseClient {

    private static SQLClient client;

    private static final JsonObject CONFIG = new JsonObject()
            .put("url", "jdbc:mariadb://db:3306/todo_app")
            .put("driver_class", "org.mariadb.jdbc.Driver")
            .put("user", "todouser")
            .put("password", "todopassword")
            .put("max_pool_size", 10);

    public static void init(Vertx vertx) {
        if (client == null) {
            client = JDBCClient.createShared(vertx, CONFIG);
            System.out.println("✅ DB connection pool initialized.");
        }
    }

    public static SQLClient getClient() {
        if (client == null) {
            throw new IllegalStateException("❌ DatabaseClient not initialized. Call init() first.");
        }
        return client;
    }
}
