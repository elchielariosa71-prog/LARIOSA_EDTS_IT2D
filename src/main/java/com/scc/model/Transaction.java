package com.scc.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public record Transaction(int transaction_id, int customer_id, int dispatcher_id,
                          String transaction_type, int quantity, double amount_charged, Timestamp transaction_date) {

    @Override
    public String toString() {
        if (transaction_date == null) return "N/A";


        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a"); // Example: Nov 22, 2026 05:05 PM
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        return sdf.format(transaction_date);
    }
}