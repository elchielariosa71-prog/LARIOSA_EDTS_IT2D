package com.scc.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public record ActivityLog(int id, int userId, String actionType, String description, Timestamp timestamp) {
    @Override
    public String toString() {
        // 1. Set the format you want to see (e.g., Nov 09, 2026 10:30:15 PM)
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

        // 2. Force the timezone to Philippine Standard Time
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        // 3. Convert the database timestamp to a formatted string
        String formattedTime = sdf.format(timestamp);

        // 4. Return the formatted table row
        return String.format("%-5d | %-8d | %-18s | %-35s | %s", id, userId, actionType, description, formattedTime);
    }
}