package com.example.simplechatapp.models;

public class User {
    public String uid;
    public String name;
    public String surname;
    public String email;
    public String bio;
    public String status;
    public String imageUrl, coverurl;


    public User() {}

    public User(String uid, String name, String surname, String email, String bio, String status, String imageUrl, String coverurl) {
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.bio=bio;
        this.status = status;
        this.imageUrl = imageUrl;
        this.coverurl=coverurl;
    }
}

