package com.scc.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public record ActivityLog(int id, int userId, String actionType, String description, Timestamp timestamp) {
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        String formattedTime = sdf.format(timestamp);

        return String.format("%-5d | %-8d | %-18s | %-35s | %s", id, userId, actionType, description, formattedTime);
    }
}