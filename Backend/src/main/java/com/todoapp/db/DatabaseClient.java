package com.todoapp.db;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

public class DatabaseClient {

    private static SQLClient client;

    public static void init(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:mariadb://db:3306/todo_app")
                .put("driver_class", "org.mariadb.jdbc.Driver")
                .put("user", "todouser")
                .put("password", "todopassword")
                .put("max_pool_size", 10);

        client = JDBCClient.createShared(vertx, config);
        System.out.println("✅ Verbindung zur Datenbank erfolgreich!");
    }

    public static SQLClient getClient() {
        return client;
    }
}
