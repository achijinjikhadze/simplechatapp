package com.example.simplechatapp.models;

public class Message {
    public String senderId;
    public String receiverId;
    public String message;
    public long timestamp;
    public boolean seen;

    public Message() {}

    public Message(String senderId, String receiverId, String message, long timestamp, boolean seen) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
    }
}
