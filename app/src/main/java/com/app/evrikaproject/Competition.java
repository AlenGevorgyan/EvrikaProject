package com.app.evrikaproject;

import java.util.List;

public class Competition {
    public String posterId;
    public String game_name;
    public String sport;
    public String type; // "public" or "private"
    public String createdBy;
    public List<String> teams;
    public List<String> invitedTeams;
    public String date;
    public int teamPlayerCount;
    public double latitude;
    public double longitude;
    public String time;
    public List<String> requests; // User IDs who requested to join (for private games)

    public Competition() {} // Firestore needs this

    public Competition(String id, String game_name, String sport, String type, String createdBy, List<String> teams, List<String> invitedTeams, String date, int teamPlayerCount, double latitude, double longitude, String time, List<String> requests) {
        this.posterId = id;
        this.game_name = game_name;
        this.sport = sport;
        this.type = type;
        this.createdBy = createdBy;
        this.teams = teams;
        this.invitedTeams = invitedTeams;
        this.date = date;
        this.teamPlayerCount = teamPlayerCount;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.requests = requests;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getTeams() {
        return teams;
    }

    public void setTeams(List<String> teams) {
        this.teams = teams;
    }

    public List<String> getInvitedTeams() {
        return invitedTeams;
    }

    public void setInvitedTeams(List<String> invitedTeams) {
        this.invitedTeams = invitedTeams;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTeamPlayerCount() {
        return teamPlayerCount;
    }

    public void setTeamPlayerCount(int teamPlayerCount) {
        this.teamPlayerCount = teamPlayerCount;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<String> getRequests() {
        return requests;
    }

    public void setRequests(List<String> requests) {
        this.requests = requests;
    }
}