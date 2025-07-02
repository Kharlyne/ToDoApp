package com.todoapp.routes;
import com.todoapp.handlers.ToDoHandler;
import io.vertx.ext.web.Router;

public class ToDoRoutes {
    public static void register(Router router, ToDoHandler handler) {
        router.post("/todos").handler(handler::createTodo);
        router.delete("/todos/:id").handler(handler::deleteTodo);
        router.put("/todos/:id").handler(handler::updateTodo);
        router.get("/todos").handler(handler::getTodos);
        router.put("/todos/:id/done").handler(handler::updateDoneStatus);
        router.get("/todos/shared").handler(handler::getSharedTodos);
        router.post("/todos/:id/share").handler(handler::shareTodo);
    }
}
