package com.app.evrikaproject;

import com.google.firebase.Timestamp;
import java.util.List;

public class ChatRoom {
    public String chatRoomId;
    public String competitionId;
    public String competitionName;
    public List<String> participantIds;
    public Timestamp createdAt;
    public Timestamp lastMessageTime;
    public String lastMessage;

    public ChatRoom() {
        // Required empty constructor for Firestore
    }

    public ChatRoom(String chatRoomId, String competitionId, String competitionName, List<String> participantIds) {
        this.chatRoomId = chatRoomId;
        this.competitionId = competitionId;
        this.competitionName = competitionName;
        this.participantIds = participantIds;
        this.createdAt = Timestamp.now();
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
} 