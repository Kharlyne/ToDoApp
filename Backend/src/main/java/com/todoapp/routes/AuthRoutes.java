package com.todoapp.routes;

import com.todoapp.handlers.AuthHandler;
import io.vertx.ext.web.Router;

public class AuthRoutes {
    public static void register(Router router, AuthHandler handler) {
        router.post("/register").handler(handler::register);
        router.post("/login").handler(handler::login);
        router.post("/logout").handler(handler::logout);
    }
}
