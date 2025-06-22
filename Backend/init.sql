CREATE DATABASE IF NOT EXISTS todo_app;
USE todo_app;

CREATE TABLE users (
                       userId INT(11) NOT NULL AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       PRIMARY KEY (userId)
);

CREATE TABLE todos (
                       todoId INT(11) NOT NULL AUTO_INCREMENT,
                       title VARCHAR(255) NOT NULL,
                       beschreibung TEXT,
                       done TINYINT(1) DEFAULT 0,
                       created_by INT(11) NOT NULL,
                       created_at TIMESTAMP DEFAULT current_timestamp(),
                       PRIMARY KEY (todoId),
                       FOREIGN KEY (created_by) REFERENCES users(userId)
                           ON DELETE CASCADE
                           ON UPDATE CASCADE
);

CREATE TABLE todo_shared (
                             shareId INT(11) NOT NULL AUTO_INCREMENT,
                             todo_id INT(11) NOT NULL,
                             user_id INT(11) NOT NULL,
                             PRIMARY KEY (shareId),
                             FOREIGN KEY (todo_id) REFERENCES todos(todoId)
                                 ON DELETE CASCADE
                                 ON UPDATE CASCADE,
                             FOREIGN KEY (user_id) REFERENCES users(userId)
                                 ON DELETE CASCADE
                                 ON UPDATE CASCADE
);
