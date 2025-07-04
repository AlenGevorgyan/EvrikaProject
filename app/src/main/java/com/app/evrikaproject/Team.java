package com.app.evrikaproject;

import java.util.List;

public class Team {
    public String id;
    public String name;
    public String createdBy;
    public List<String> members;
    public String inviteCode;

    public Team() {} // Firestore needs this

    public Team(String id, String name, String createdBy, List<String> members, String inviteCode) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.members = members;
        this.inviteCode = inviteCode;
    }
} 