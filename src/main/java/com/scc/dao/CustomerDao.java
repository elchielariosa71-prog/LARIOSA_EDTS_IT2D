package com.scc.dao;

import com.scc.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public boolean addCustomer(int dispatcherId, String fullName, String address, String contact, String subType) {
        String sql = "INSERT INTO customers(full_name, address, contact_number, subscription_type, account_status, outstanding_balance) VALUES (?, ?, ?, ?, 'ACTIVE', 0.0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, address);
            pstmt.setString(3, contact);
            pstmt.setString(4, subType.toUpperCase());
            pstmt.executeUpdate();


            logDao.logAction(dispatcherId, "CUSTOMER_ADDED", "Added new customer: " + fullName);
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }

    public List<Customer> getAllActiveCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE account_status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("subscription_type"),
                        rs.getString("account_status"),
                        rs.getDouble("outstanding_balance")
                ));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public boolean updateBalance(int customerId, double amountToAdd) {
        String sql = "UPDATE customers SET outstanding_balance = outstanding_balance + ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amountToAdd);
            pstmt.setInt(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCustomerDetails(int id, String address, String subType) {
        String sql = "UPDATE customers SET address = ?, subscription_type = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            pstmt.setString(2, subType);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }

    public boolean deactivateCustomer(int id) {
        String sql = "UPDATE customers SET account_status = 'INACTIVE' WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating customer: " + e.getMessage());
            return false;
        }
    }
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ? AND account_status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("full_name"),
                            rs.getString("address"),
                            rs.getString("contact_number"),
                            rs.getString("subscription_type"),
                            rs.getString("account_status"),
                            rs.getDouble("outstanding_balance")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer: " + e.getMessage());
        }
        return null;
    }


    public boolean processPayment(int customerId, double amountPaid) {
        String sql = "UPDATE customers SET outstanding_balance = outstanding_balance - ? WHERE customer_id = ? AND outstanding_balance >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amountPaid);
            pstmt.setInt(2, customerId);
            pstmt.setDouble(3, amountPaid);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }

    public List<Customer> getCustomersWithBalance() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE outstanding_balance > 0 AND account_status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("subscription_type"),
                        rs.getString("account_status"),
                        rs.getDouble("outstanding_balance")
                ));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public int getContainersOut(int customerId) {
        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE customer_id = ? AND transaction_type = 'DELIVERY') - " +
                "(SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE customer_id = ? AND transaction_type = 'RETURN') " +
                "AS containers_out";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("containers_out");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating containers out: " + e.getMessage());
        }
        return 0;
    }
}
