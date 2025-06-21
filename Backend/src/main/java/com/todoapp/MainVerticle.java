package com.todoapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Launcher;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.Set;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class MainVerticle extends AbstractVerticle {

    private SQLClient jdbc;

    public static void main(String[] args) {
        Launcher.executeCommand("run", MainVerticle.class.getName());
    }

    @Override
  public void start(Promise<Void> startPromise) {
    // Configuration DB
    JsonObject config = new JsonObject()
        .put("url", "jdbc:mariadb://db:3306/todo")
        .put("driver_class", "org.mariadb.jdbc.Driver")
        .put("user", "todouser")
        .put("password", "todopassword")
        .put("max_pool_size", 10);

        jdbc = JDBCClient.createShared(vertx, config);

        // Test connexion DB
        jdbc.getConnection(ar -> {
            if (ar.succeeded()) {
                System.out.println("✅ Verbindung zur Datenbank erfolgreich!");
                ar.result().close();
            } else {
                System.out.println("❌ DB connection failed: " + ar.cause().getMessage());
            }
        });

        Router router = Router.router(vertx);

        // Middlewares
        router.route()
            .handler(CorsHandler.create("*")
                .allowedMethods(Set.of(
                    HttpMethod.GET,
                    HttpMethod.POST, 
                    HttpMethod.PUT,
                    HttpMethod.DELETE,
                    HttpMethod.OPTIONS))
                .allowedHeaders(Set.of("Content-Type", "Authorization"))
                .allowCredentials(true))
            .handler(BodyHandler.create());

        // Routes
        router.get("/").handler(ctx -> {
    ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("Server", "läuft").encode());
});

        router.get("/api/todos").handler(ctx -> {
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonArray().encode());
        });
  // Login und Logout Routen registrieren
router.post("/login").handler(this::login);
router.post("/logout").handler(this::logout);
// Registrierung-Route
router.post("/register").handler(this::register);
router.post("/todos").handler(this::createTodo);
router.delete("/todos/:id").handler(this::deleteTodo);
router.put("/todos/:id").handler(this::updateTodo);
router.get("/todos/shared").handler(this::getSharedTodos);
router.put("/todos/:id/done").handler(this::updateDoneStatus);
router.get("/todos").handler(this::getTodos);
router.put("/todos/:id/done").handler(this::todoDoneStatus);



router.post("/todos/:id/share").handler(this::shareTodo);router.get("/debug/session").handler(ctx -> {
  JsonObject sessionData = new JsonObject();
  ctx.session().data().forEach((key, value) -> {sessionData.put(key, value.toString()); });
  ctx.response()
     .putHeader("Content-Type", "application/json")
     .end(sessionData.encodePrettily());
});
router.get("/debug/session").handler(ctx -> {
  JsonObject sessionData = new JsonObject();
  ctx.session().data().forEach((key, value) -> {
    sessionData.put(key, value.toString());
  });
  ctx.response()
     .putHeader("Content-Type", "application/json")
     .end(sessionData.encodePrettily());
});

// 5. Starten des HTTP-Servers
    vertx.createHttpServer()
      .requestHandler(router) // Router als Handler für eingehende Anfragen
      .listen(8888, http -> { // Port 8888 für den Server
        if (http.succeeded()) {
          System.out.println("✅ HTTP-Server läuft auf Port 8888!");
          startPromise.complete(); // Verticle erfolgreich gestartet
        } else {
          System.out.println("❌ Fehler beim Starten des HTTP-Servers: " + http.cause().getMessage());
          startPromise.fail(http.cause()); // Verticle konnte nicht gestartet werden
        }
      });
  }


// Verarbeitet die Login-Anfrage eines Benutzers.
 

private void login(RoutingContext ctx) {
  JsonObject body = ctx.body().asJsonObject();
  String username = body.getString("username");
  String password = body.getString("password");

  jdbc.getConnection(dbConn -> {
    if (dbConn.failed()) {
      ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
      return;
    }

    var connection = dbConn.result();
    String sql = "SELECT * FROM users WHERE username = ?";

    connection.queryWithParams(sql, new JsonArray().add(username), queryRes -> {
      if (queryRes.succeeded()) {
        if (queryRes.result().getNumRows() > 0) {
          JsonObject user = queryRes.result().getRows().get(0);
          String hashedPassword = user.getString("password");

          BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
          if (result.verified) {
            int userId = user.getInteger("userId");
            ctx.session().put("userId", userId); // stocke dans la session

            // DEBUG (optionnel)
            System.out.println("✅ Login OK — session ID: " + ctx.session().id());

            ctx.response()
              .setStatusCode(200)
              .putHeader("Content-Type", "application/json")
              // 👉 ENVOI EXPLICITE du cookie de session
              .putHeader("Set-Cookie", "vertx-web.session=" + ctx.session().id() + "; Path=/; HttpOnly")
              .end(new JsonObject()
                  .put("status", "ok")
                  .put("message", "Login erfolgreich!")
                  .put("userID", userId)
                  .encode());
          } else {
            ctx.response().setStatusCode(401).end(new JsonObject().put("message", "Ungültiger Benutzername oder Passwort").encode());
          }
        } else {
          ctx.response().setStatusCode(401).end(new JsonObject().put("message", "Benutzer nicht gefunden").encode());
        }
      } else {
        ctx.response().setStatusCode(500).end("Fehler bei der Datenbankabfrage");
      }
      connection.close();
    });
  });
}

/**
 * Verarbeitet die Logout-Anfrage eines Benutzers.
 */
private void logout(RoutingContext ctx) {
  if (ctx.session() != null && !ctx.session().isDestroyed()) {
    ctx.session().destroy();
    ctx.response().putHeader("content-type", "application/json")
      .end(new JsonObject().put("status", "ok").put("message", "Logout erfolgreich!").encode());
  } else {
    ctx.response().putHeader("content-type", "application/json")
      .end(new JsonObject().put("status", "ok").put("message", "Keine aktive Session!").encode());
  }
}

/**
 * Verarbeitet die Registrierung eines neuen Benutzers.
 */
private void register(RoutingContext ctx) {
  JsonObject body = ctx.body().asJsonObject(); // JSON-Daten auslesen
  String username = body.getString("username");
  String password = body.getString("password");

  if (username == null || password == null) {
    ctx.response().setStatusCode(400).end("Benutzername und Passwort erforderlich");
    return;
  }

  jdbc.getConnection(dbConn -> {
    if (dbConn.failed()) {
      ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
      return;
    }

  var connection = dbConn.result();

    String checkQuery = "SELECT * FROM users WHERE username = ?";
    connection.queryWithParams(checkQuery, new io.vertx.core.json.JsonArray().add(username), checkRes -> {
      if (checkRes.succeeded()) {
        if (checkRes.result().getNumRows() > 0) {
          ctx.response().setStatusCode(409)
            .end(new JsonObject().put("status", "error").put("message", "Benutzername bereits vergeben").encode());
          connection.close();
        } else {
          // Passwort hashen!
          String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

          String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
          connection.updateWithParams(insertQuery, new io.vertx.core.json.JsonArray().add(username).add(hashedPassword), insertRes -> {
            if (insertRes.succeeded()) {
              ctx.response().putHeader("content-type", "application/json")
                .end(new JsonObject().put("status", "ok").put("message", "Registrierung erfolgreich!").encode());
            } else {
              ctx.response().setStatusCode(500).end("Fehler beim Einfügen des Benutzers");
            }
            connection.close();
          });
        }
      } else {
        ctx.response().setStatusCode(500).end("Fehler bei der Datenbankabfrage");
        connection.close();
      }
    });
  });
}
private void createTodo(RoutingContext ctx) {
  JsonObject body = ctx.body().asJsonObject(); // Body auslesen
  String title = body.getString("title");
  String beschreibung = body.getString("beschreibung", ""); // Optionales Feld

  // Benutzer-ID aus Session holen
  Integer userId = ctx.session().get("userId");
  System.out.println("📌 [DEBUG] userId in session: " + userId);


  if (userId == null) {
    ctx.response().setStatusCode(401).end("Nicht eingeloggt");
    return;
  }

  if (title == null || title.isEmpty()) {
    ctx.response().setStatusCode(400).end("Titel erforderlich");
    return;
  }

  jdbc.getConnection(dbConn -> {
    if (dbConn.failed()) {
      ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
      return;
    }

    var connection = dbConn.result();
    String insertQuery = "INSERT INTO todos (created_by, title, beschreibung) VALUES (?, ?, ?)";

    connection.updateWithParams(insertQuery,
      new io.vertx.core.json.JsonArray().add(userId).add(title).add(beschreibung),
      res -> {
        if (res.succeeded()) {
          ctx.response().putHeader("content-type", "application/json")
            .end(new JsonObject().put("status", "ok").put("message", "ToDo erstellt!").encode());
        } else {
          ctx.response().setStatusCode(500).end("Fehler beim Erstellen des ToDos");
        }
        connection.close();
      }
    );
  });
}
private void deleteTodo(RoutingContext ctx) {
  Integer userId = ctx.session().get("userId"); 
  if (userId == null) {
    ctx.response().setStatusCode(401).end("Nicht eingeloggt");
    return;
  }

  String idParam = ctx.pathParam("id");
  if (idParam == null) {
    ctx.response().setStatusCode(400).end("ID erforderlich");
    return;
  }

  int todoId;
  try {
    todoId = Integer.parseInt(idParam);
  } catch (NumberFormatException e) {
    ctx.response().setStatusCode(400).end("Ungültige ID");
    return;
  }

  jdbc.getConnection(dbConn -> {
    if (dbConn.failed()) {
      ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
      return;
    }

    var connection = dbConn.result();
    // Correction : les bons noms de colonnes de ta base !
    String deleteQuery = "DELETE FROM todos WHERE todoId = ? AND created_by = ?";

    connection.updateWithParams(deleteQuery,
      new io.vertx.core.json.JsonArray().add(todoId).add(userId),
      res -> {
        if (res.succeeded()) {
          if (res.result().getUpdated() > 0) { // Vérifier qu'on a bien supprimé quelque chose
            ctx.response().putHeader("content-type", "application/json")
              .end(new JsonObject().put("status", "ok").put("message", "ToDo gelöscht!").encode());
          } else {
            ctx.response().setStatusCode(404)
              .end(new JsonObject().put("status", "error").put("message", "ToDo nicht gefunden oder keine Berechtigung!").encode());
          }
        } else {
          ctx.response().setStatusCode(500).end("Fehler beim Löschen der ToDo");
        }
        connection.close();
      }
    );
  });
}

private void updateTodo(RoutingContext ctx) {
  Integer userId = ctx.session().get("userId"); // 🟢 Cohérence avec create/delete
  if (userId == null) {
    ctx.response().setStatusCode(401).end("Nicht eingeloggt");
    return;
  }

  String idParam = ctx.pathParam("id");
  if (idParam == null) {
    ctx.response().setStatusCode(400).end("ID erforderlich");
    return;
  }

  int todoId;
  try {
    todoId = Integer.parseInt(idParam);
  } catch (NumberFormatException e) {
    ctx.response().setStatusCode(400).end("Ungültige ID");
    return;
  }

  JsonObject body = ctx.body().asJsonObject();
  String title = body.getString("title");
  String beschreibung = body.getString("beschreibung"); // 👈 On garde "description" côté client

  if (title == null || title.isEmpty()) {
    ctx.response().setStatusCode(400).end("Titel erforderlich");
    return;
  }

  jdbc.getConnection(dbConn -> {
    if (dbConn.failed()) {
      ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
      return;
    }

    var connection = dbConn.result();
    String updateQuery = "UPDATE todos SET title = ?, beschreibung = ? WHERE todoId = ? AND created_by = ?";

    connection.updateWithParams(updateQuery,
      new io.vertx.core.json.JsonArray().add(title).add(beschreibung).add(todoId).add(userId),
      res -> {
        if (res.succeeded()) {
          if (res.result().getUpdated() > 0) {
            ctx.response().putHeader("content-type", "application/json")
              .end(new JsonObject().put("status", "ok").put("message", "ToDo aktualisiert!").encode());
          } else {
            ctx.response().setStatusCode(404)
              .end(new JsonObject().put("status", "error").put("message", "ToDo nicht gefunden oder keine Berechtigung!").encode());
          }
        } else {
          ctx.response().setStatusCode(500).end("Fehler beim Aktualisieren der ToDo");
        }
        connection.close();
      }
    );
  });
}
private void getTodos(RoutingContext ctx) {
  Integer userId = ctx.session().get("userId");

  if (userId == null) {
      ctx.response().setStatusCode(401)
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("error", "Nicht eingeloggt").encode());
      return;
  }

  jdbc.getConnection(connRes -> {
      if (connRes.failed()) {
          ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
          return;
      }

      var conn = connRes.result();
      String query = "SELECT todoId, title, beschreibung, done FROM todos WHERE created_by = ?";

      conn.queryWithParams(query, new JsonArray().add(userId), res -> {
          if (res.succeeded()) {
              var rows = res.result().getRows();
              ctx.response()
                  .putHeader("Content-Type", "application/json")
                  .end(new JsonObject().put("todos", rows).encode());
          } else {
              ctx.response().setStatusCode(500).end("Fehler beim Laden der ToDos");
          }
          conn.close();
      });
  });
}
private void todoDoneStatus(RoutingContext ctx) {
  Integer userId = ctx.session().get("userId");

  if (userId == null) {
      ctx.response().setStatusCode(401).end("Nicht eingeloggt");
      return;
  }

  String idParam = ctx.pathParam("id");
  int todoId;
  try {
      todoId = Integer.parseInt(idParam);
  } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end("Ungültige ID");
      return;
  }

  JsonObject body = ctx.body().asJsonObject();
  boolean done = body.getBoolean("done", false); // false par défaut

  String updateQuery = "UPDATE todos SET done = ? WHERE todoId = ? AND created_by = ?";

  jdbc.getConnection(connRes -> {
      if (connRes.failed()) {
          ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
          return;
      }

      var conn = connRes.result();

      conn.updateWithParams(
          updateQuery,
          new JsonArray().add(done ? 1 : 0).add(todoId).add(userId),
          res -> {
              if (res.succeeded()) {
                  ctx.response().putHeader("Content-Type", "application/json")
                      .end(new JsonObject().put("status", "ok").put("message", "ToDo aktualisiert!").encode());
              } else {
                  ctx.response().setStatusCode(500).end("Fehler beim Aktualisieren");
              }
              conn.close();
          }
      );
  });
}




private void getSharedTodos(RoutingContext ctx) {
  Integer userID = ctx.session().get("userID");
  if (userID == null) {
      ctx.response().setStatusCode(401).end("Nicht eingeloggt");
      return;
  }

  jdbc.getConnection(dbConn -> {
      if (dbConn.failed()) {
          ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
          return;
      }

      var connection = dbConn.result();
      String selectQuery = "SELECT * FROM todos WHERE shared = true AND userID != ?";

      connection.queryWithParams(selectQuery, new io.vertx.core.json.JsonArray().add(userID), res -> {
          if (res.succeeded()) {
              ctx.response().putHeader("content-type", "application/json")
                  .end(new JsonObject().put("status", "ok").put("todos", res.result().getRows()).encode());
          } else {
              ctx.response().setStatusCode(500).end("Fehler beim Abrufen der ToDos");
          }
          connection.close();
      });
  });

}
private void shareTodo(RoutingContext ctx) {
  Integer ownerID = ctx.session().get("userID");
  if (ownerID == null) {
      ctx.response().setStatusCode(401).end("Nicht eingeloggt");
      return;
  }

  String todoIdParam = ctx.pathParam("id");
  if (todoIdParam == null) {
      ctx.response().setStatusCode(400).end("ID erforderlich");
      return;
  }

  int todoId;
  try {
      todoId = Integer.parseInt(todoIdParam);
  } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end("Ungültige ID");
      return;
  }

  JsonObject body = ctx.body().asJsonObject();
  String targetUsername = body.getString("username");
  if (targetUsername == null) {
      ctx.response().setStatusCode(400).end("Username erforderlich");
      return;
  }

  jdbc.getConnection(dbConn -> {
      if (dbConn.failed()) {
          ctx.response().setStatusCode(500).end("Datenbankverbindung fehlgeschlagen");
          return;
      }

      var connection = dbConn.result();

      // Erst prüfen, ob das ToDo wirklich dem Benutzer gehört
      String checkTodo = "SELECT id FROM todos WHERE id = ? AND userID = ?";
      connection.queryWithParams(checkTodo, new io.vertx.core.json.JsonArray().add(todoId).add(ownerID), todoRes -> {
          if (todoRes.succeeded() && todoRes.result().getNumRows() > 0) {
              // Benutzer-ID vom Username holen
              String getUser = "SELECT userID FROM users WHERE username = ?";
              connection.queryWithParams(getUser, new io.vertx.core.json.JsonArray().add(targetUsername), userRes -> {
                  if (userRes.succeeded() && userRes.result().getNumRows() > 0) {
                      int targetUserID = userRes.result().getRows().get(0).getInteger("userID");

                      // Teilen in todo_shared_users einfügen
                      String insertShare = "INSERT INTO todo_shared_users (todo_id, user_id) VALUES (?, ?)";
                      connection.updateWithParams(insertShare, new io.vertx.core.json.JsonArray().add(todoId).add(targetUserID), shareRes -> {
                          if (shareRes.succeeded()) {
                              ctx.response().putHeader("content-type", "application/json")
                                  .end(new JsonObject().put("status", "ok").put("message", "Todo geteilt!").encode());
                          } else {
                              ctx.response().setStatusCode(500).end("Fehler beim Teilen der ToDo");
                          }
                          connection.close();
                      });

                  } else {
                      ctx.response().setStatusCode(404).end("Benutzer nicht gefunden");
                      connection.close();
                  }
              });
          } else {
              ctx.response().setStatusCode(403).end("Keine Berechtigung, dieses ToDo zu teilen");
              connection.close();
          }
      });
  });
}
private void updateDoneStatus(RoutingContext ctx) {
  Integer userId = ctx.session().get("userId");
  if (userId == null) {
    ctx.response().setStatusCode(401).end("Nicht eingeloggt");
    return;
  }

  String idParam = ctx.pathParam("id");
  if (idParam == null) {
    ctx.response().setStatusCode(400).end("ID erforderlich");
    return;
  }

  int todoId;
  try {
    todoId = Integer.parseInt(idParam);
  } catch (NumberFormatException e) {
    ctx.response().setStatusCode(400).end("Ungültige ID");
    return;
  }

  JsonObject body = ctx.body().asJsonObject();
  Boolean done = body.getBoolean("done");
  if (done == null) {
    ctx.response().setStatusCode(400).end("Feld 'done' erforderlich");
    return;
  }

  jdbc.getConnection(dbConn -> {
    if (dbConn.failed()) {
      ctx.response().setStatusCode(500).end("Datenbankfehler");
      return;
    }

    var connection = dbConn.result();
    String updateQuery = "UPDATE todos SET done = ? WHERE todoId = ? AND created_by = ?";

    connection.updateWithParams(updateQuery,
      new io.vertx.core.json.JsonArray().add(done).add(todoId).add(userId),
      res -> {
        if (res.succeeded() && res.result().getUpdated() > 0) {
          ctx.response().putHeader("Content-Type", "application/json")
            .end(new JsonObject().put("status", "ok").put("message", "Aufgabe aktualisiert").encode());
        } else {
          ctx.response().setStatusCode(404)
            .end(new JsonObject().put("status", "error").put("message", "Nicht gefunden oder keine Berechtigung").encode());
        }
        connection.close();
      });
  });
}


}



