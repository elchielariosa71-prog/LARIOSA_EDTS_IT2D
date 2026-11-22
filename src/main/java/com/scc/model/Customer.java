package com.scc.model;

public record Customer(int customer_id, String full_name, String address, String contact_number,
                       String subscription_type, String account_status, double outstanding_balance) {
    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | Status: %s | Balance: ₱%.2f",
                customer_id, full_name, subscription_type, account_status, outstanding_balance);
    }
}