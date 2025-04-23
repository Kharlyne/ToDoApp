package com.todoapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.createHttpServer()
      .requestHandler(req -> {
        req.response()
           .putHeader("content-type", "text/plain")
           .end("Hello from Vert.x backend!");
      })
      .listen(8888, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("✅ Serveur Vert.x lancé sur http://localhost:8888");
        } else {
          startPromise.fail(http.cause());
        }
      });
  }
  public static void main(String[] args) {
    io.vertx.core.Launcher.executeCommand("run", MainVerticle.class.getName());
  }
  
}
