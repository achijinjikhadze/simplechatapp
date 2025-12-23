package com.example.simplechatapp.models;

public class User {
    public String uid;
    public String name;
    public String email;
    public String status;

    public User() {}

    public User(String uid, String name, String email, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.status = status;
    }
}
