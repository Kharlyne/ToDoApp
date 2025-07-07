package com.todoapp.routes;

import com.todoapp.handlers.AuthHandler;
import io.vertx.ext.web.Router;

public class AuthRoutes {
    public static Router createSubRouter(Router root, AuthHandler handler) {
        Router sub = root.getDelegate().router();

        sub.post("/register").handler(handler::register);
        sub.post("/login").handler(handler::login);
        sub.post("/logout").handler(handler::logout);

        return sub;
    }
}
