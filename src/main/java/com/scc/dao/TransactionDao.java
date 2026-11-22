package com.scc.dao;

import com.scc.model.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    public List<Transaction> getTransactionsByCustomerId(int customerId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE customer_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("dispatcher_id"),
                            rs.getString("transaction_type"),
                            rs.getInt("quantity"),
                            rs.getDouble("amount_charged"),
                            rs.getTimestamp("transaction_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }
}