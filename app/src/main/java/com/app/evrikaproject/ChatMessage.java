package com.app.evrikaproject;

import com.google.firebase.Timestamp;

public class ChatMessage {
    public String messageId;
    public String senderId;
    public String senderName;
    public String message;
    public Timestamp timestamp;

    public ChatMessage() {
        // Required empty constructor for Firestore
    }

    public ChatMessage(String messageId, String senderId, String senderName, String message) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = Timestamp.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


} 