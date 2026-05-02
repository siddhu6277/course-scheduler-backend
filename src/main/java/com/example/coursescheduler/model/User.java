package com.example.coursescheduler.model;

public class User {
    public String id;
    public String username;
    public String password;
    public String name;
    public String email;
    public String role;

    public User() {
    }

    public User(String id, String username, String password, String name, String email, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
