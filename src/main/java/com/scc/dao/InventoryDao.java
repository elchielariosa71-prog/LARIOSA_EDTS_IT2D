package com.scc.dao;

import com.scc.model.Inventory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public List<Inventory> getDashboard() {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_stock";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Inventory(
                        rs.getInt("inventory_id"),
                        rs.getString("container_type"),
                        rs.getInt("quantity_full"),
                        rs.getInt("quantity_empty")
                ));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public boolean processReturn(int customerId, int dispatcherId, int qty, String containerType) {
        String updateInv = "UPDATE inventory_stock SET quantity_empty = quantity_empty + ? WHERE container_type = ?";

        String insertTrans = "INSERT INTO transactions(customer_id, dispatcher_id, transaction_type, quantity, amount_charged) VALUES (?, ?, 'RETURN', ?, 0.0)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(updateInv);
                 PreparedStatement pstmt2 = conn.prepareStatement(insertTrans)) {

                pstmt1.setInt(1, qty);
                pstmt1.setString(2, containerType);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, customerId);
                pstmt2.setInt(2, dispatcherId);
                pstmt2.setInt(3, qty);
                pstmt2.executeUpdate();

                conn.commit();
                logDao.logAction(dispatcherId, "CONTAINER_RETURNED", "Processed return of " + qty + " " + containerType + " for Customer ID " + customerId);
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Transaction failed: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    public boolean processDelivery(int customerId, int dispatcherId, int qty, String containerType, double amount) {

        String updateInv = "UPDATE inventory_stock SET quantity_full = quantity_full - ? WHERE container_type = ? AND quantity_full >= ?";


        String insertTrans = "INSERT INTO transactions(customer_id, dispatcher_id, transaction_type, quantity, amount_charged) VALUES (?, ?, 'DELIVERY', ?, ?)";

        String updateBal = "UPDATE customers SET outstanding_balance = outstanding_balance + ? WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(updateInv);
                 PreparedStatement pstmt2 = conn.prepareStatement(insertTrans);
                 PreparedStatement pstmt3 = conn.prepareStatement(updateBal)) {


                pstmt1.setInt(1, qty);
                pstmt1.setString(2, containerType);
                pstmt1.setInt(3, qty);
                int rowsInv = pstmt1.executeUpdate();


                if (rowsInv == 0) {
                    System.out.println("-> Error: Not enough " + containerType + " stock in warehouse!");
                    conn.rollback();
                    return false;
                }

                pstmt2.setInt(1, customerId);
                pstmt2.setInt(2, dispatcherId);
                pstmt2.setInt(3, qty);
                pstmt2.setDouble(4, amount);
                pstmt2.executeUpdate();

                pstmt3.setDouble(1, amount);
                pstmt3.setInt(2, customerId);
                pstmt3.executeUpdate();

                conn.commit();
                logDao.logAction(dispatcherId, "DELIVERY_LOGGED", "Delivered " + qty + " " + containerType + " to Customer ID " + customerId + " (₱" + amount + ")");
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Transaction failed: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }
}
