package com.todoapp.db;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
public class DatabaseClient {

    private static final Logger logger = Logger.getLogger(DatabaseClient.class.getName());
    private static SQLClient client;

static {
    try {
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
        FileHandler fileHandler = new FileHandler("logs/db.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(true); // log aussi dans le terminal
    } catch (Exception e) {
        System.err.println("❌ Logger setup failed in DatabaseClient: " + e.getMessage());
    }
}
    public static void init(Vertx vertx) {
        if (client == null) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String host = dotenv.get("DB_HOST", "localhost");
            String port = dotenv.get("DB_PORT", "3306");
            String dbName = dotenv.get("DB_NAME", "todo_app");
            String user = dotenv.get("DB_USER", "root");
            String password = dotenv.get("DB_PASSWORD", "");
            int poolSize = Integer.parseInt(dotenv.get("DB_POOL_SIZE", "10"));

            String jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + dbName;

            JsonObject config = new JsonObject()
                    .put("url", jdbcUrl)
                    .put("driver_class", "org.mariadb.jdbc.Driver")
                    .put("user", user)
                    .put("password", password)
                    .put("max_pool_size", poolSize);

            client = JDBCClient.createShared(vertx, config);

            logger.info("✅ DB connection pool initialized.");
            System.out.println("✅ DB connection pool initialized.");
        }
    }

    public static SQLClient getClient() {
        if (client == null) {
             logger.severe("❌ Attempted to access DB client before initialization.");
            throw new IllegalStateException("❌ DatabaseClient not initialized. Call init() first.");
        }
        return client;
    }
}
