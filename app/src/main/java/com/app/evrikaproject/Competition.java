package com.app.evrikaproject;

import java.util.List;

public class Competition {
    public String id;
    public String name;
    public String sport;
    public String type; // "public" or "private"
    public String createdBy;
    public List<String> teams;
    public List<String> invitedTeams;

    public Competition() {} // Firestore needs this

    public Competition(String id, String name, String sport, String type, String createdBy, List<String> teams, List<String> invitedTeams) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.type = type;
        this.createdBy = createdBy;
        this.teams = teams;
        this.invitedTeams = invitedTeams;
    }
} 