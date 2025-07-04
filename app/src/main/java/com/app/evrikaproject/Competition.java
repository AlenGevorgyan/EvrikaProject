package com.app.evrikaproject;

import java.util.List;

public class Competition {
    public String posterId;
    public String name;
    public String sport;
    public String type; // "public" or "private"
    public String createdBy;
    public List<String> teams;
    public List<String> invitedTeams;
    public String date;
    public int teamPlayerCount;
    public double latitude;
    public double longitude;

    public Competition() {} // Firestore needs this

    public Competition(String id, String name, String sport, String type, String createdBy, List<String> teams, List<String> invitedTeams, String date, int teamPlayerCount, double latitude, double longitude) {
        this.posterId = id;
        this.name = name;
        this.sport = sport;
        this.type = type;
        this.createdBy = createdBy;
        this.teams = teams;
        this.invitedTeams = invitedTeams;
        this.date = date;
        this.teamPlayerCount = teamPlayerCount;
        this.latitude = latitude;
        this.longitude = longitude;
    }
} 