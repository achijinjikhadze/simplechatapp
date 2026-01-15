package com.example.simplechatapp.models;

public class Message {
    public String senderId;
    public String receiverId;
    public String message;
    public String imageurl;
    public long timestamp;
    public boolean seen;
    public String key;

    public Message() {}

    public Message(String senderId, String receiverId, String message, String imageurl, long timestamp, boolean seen) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.imageurl=imageurl;
        this.timestamp = timestamp;
        this.seen = seen;
    }
}
