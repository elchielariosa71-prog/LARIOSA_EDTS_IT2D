package com.scc.dao;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class ReportDao {

    public void generateMonthlyReport(int month, int year) {
        String sqlRevenue = "SELECT SUM(amount_charged) as revenue FROM transactions WHERE transaction_type = 'DELIVERY' AND strftime('%m', transaction_date) = ? AND strftime('%Y', transaction_date) = ?";
        String sqlReturns = "SELECT SUM(quantity) as returned FROM transactions WHERE transaction_type = 'RETURN' AND strftime('%m', transaction_date) = ? AND strftime('%Y', transaction_date) = ?";
        String sqlCirculation = "SELECT SUM(quantity) as in_circulation FROM transactions WHERE transaction_type = 'DELIVERY' AND strftime('%m', transaction_date) = ? AND strftime('%Y', transaction_date) = ?";

        String strMonth = String.format("%02d", month);
        String strYear = String.valueOf(year);

        double revenue = 0.0;
        int returned = 0;
        int circulation = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {


            try (PreparedStatement pstmt = conn.prepareStatement(sqlRevenue)) {
                pstmt.setString(1, strMonth);
                pstmt.setString(2, strYear);
                ResultSet rs = pstmt.executeQuery();
                revenue = rs.next() ? rs.getDouble("revenue") : 0.0;
            }


            try (PreparedStatement pstmt = conn.prepareStatement(sqlReturns)) {
                pstmt.setString(1, strMonth);
                pstmt.setString(2, strYear);
                ResultSet rs = pstmt.executeQuery();
                returned = rs.next() ? rs.getInt("returned") : 0;
            }


            try (PreparedStatement pstmt = conn.prepareStatement(sqlCirculation)) {
                pstmt.setString(1, strMonth);
                pstmt.setString(2, strYear);
                ResultSet rs = pstmt.executeQuery();
                circulation = rs.next() ? rs.getInt("in_circulation") : 0;
            }

            System.out.println("\n==================================================");
            System.out.println("   MONTHLY CONTAINER CIRCULATION & REVENUE REPORT ");
            System.out.println("   Month: " + strMonth + " | Year: " + strYear);
            System.out.println("==================================================");
            System.out.printf("Total Revenue Generated: ₱%.2f%n", revenue);
            System.out.println("Total Containers Returned: " + returned);
            System.out.println("Total Containers Delivered (In Circulation): " + circulation);
            System.out.println("==================================================");


            exportToCSV(strMonth, strYear, revenue, returned, circulation);

        } catch (SQLException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    private void exportToCSV(String month, String year, double revenue, int returned, int circulation) {
        String fileName = "Monthly_Report_" + year + "_" + month + ".csv";

        try (FileWriter writer = new FileWriter(fileName)) {

            writer.append("EcoDeliver Track - Monthly Report\n");
            writer.append("Month,").append(month).append("\n");
            writer.append("Year,").append(year).append("\n\n");


            writer.append("Metric,Value\n");
            writer.append("Total Revenue Generated,").append(String.format("%.2f", revenue)).append("\n");
            writer.append("Total Containers Returned,").append(String.valueOf(returned)).append("\n");
            writer.append("Total Containers Delivered,").append(String.valueOf(circulation)).append("\n");

            System.out.println("\n-> Success! Report has been exported to '" + fileName + "'");
            System.out.println("-> You can open this file in Microsoft Excel or Google Sheets.");

        } catch (IOException e) {
            System.err.println("-> Error exporting to CSV: " + e.getMessage());
        }
    }
}