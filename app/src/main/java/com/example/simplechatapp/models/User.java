package com.example.simplechatapp.models;

public class User {
    public String uid;
    public String name;
    public String email;
    public String status;
    public String imageUrl, coverurl;


    public User() {}

    public User(String uid, String name, String email, String status, String imageUrl, String coverurl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.status = status;
        this.imageUrl = imageUrl;
        this.coverurl=coverurl;
    }
}

