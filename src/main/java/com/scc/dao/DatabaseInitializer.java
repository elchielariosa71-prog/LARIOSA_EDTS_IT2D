package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL
            );
        """;


        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action_type TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp DATETIME DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
        """;


        String createCustomersTable = """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                address TEXT NOT NULL,
                contact_number TEXT,
                subscription_type TEXT NOT NULL, 
                account_status TEXT NOT NULL DEFAULT 'ACTIVE', 
                outstanding_balance REAL DEFAULT 0.0
            );
        """;


        String createInventoryTable = """
            CREATE TABLE IF NOT EXISTS inventory_stock (
                inventory_id INTEGER PRIMARY KEY AUTOINCREMENT,
                container_type TEXT UNIQUE NOT NULL, 
                quantity_full INTEGER DEFAULT 0,
                quantity_empty INTEGER DEFAULT 0,
                last_updated DATETIME DEFAULT (datetime('now', 'localtime'))
            );
        """;


        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                dispatcher_id INTEGER NOT NULL,
                transaction_type TEXT NOT NULL, 
                quantity INTEGER NOT NULL,
                amount_charged REAL DEFAULT 0.0,
                transaction_date DATETIME DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(customer_id) REFERENCES customers(customer_id),
                FOREIGN KEY(dispatcher_id) REFERENCES users(user_id)
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {


            stmt.execute(createUsersTable);
            stmt.execute(createLogsTable);
            stmt.execute(createCustomersTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createTransactionsTable);



            String seedInventory = "INSERT INTO inventory_stock (container_type, quantity_full, quantity_empty) " +
                    "SELECT '5-Gallon Round', 100, 0 WHERE NOT EXISTS (SELECT 1 FROM inventory_stock)";
            stmt.execute(seedInventory);

            String seedInventory2 = "INSERT INTO inventory_stock (container_type, quantity_full, quantity_empty) " +
                    "SELECT '5-Gallon Slim', 50, 0 WHERE (SELECT COUNT(*) FROM inventory_stock) = 1";
            stmt.execute(seedInventory2);


            System.out.println("Database tables verified/created.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}