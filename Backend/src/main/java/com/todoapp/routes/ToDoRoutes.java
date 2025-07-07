package com.todoapp.routes;

import com.todoapp.handlers.ToDoHandler;
import io.vertx.ext.web.Router;

public class ToDoRoutes {
    public static Router createSubRouter(Router root, ToDoHandler handler) {
        Router sub = root.getDelegate().router();

        sub.post("/").handler(handler::createTodo);
        sub.delete("/:id").handler(handler::deleteTodo);
        sub.put("/:id").handler(handler::updateTodo);
        sub.get("/").handler(handler::getTodos);
        sub.put("/:id/done").handler(handler::updateDoneStatus);
        sub.get("/shared").handler(handler::getSharedTodos);
        sub.post("/:id/share").handler(handler::shareTodo);

        return sub;
    }
}
