package com.example.simplechatapp.models;

public class User {
    public String uid;
    public String name;
    public String email;
    public String status;
    public String imageUrl;

    public User() {}

    public User(String uid, String name, String email, String status, String imageUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.status = status;
        this.imageUrl = imageUrl;
    }
}

