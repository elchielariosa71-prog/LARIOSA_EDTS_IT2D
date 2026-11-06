package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final ActivityLogDao logDao = new ActivityLogDao();

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n============================");
            System.out.println("     ADMIN DASHBOARD        ");
            System.out.println("============================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Approvals");
            System.out.println("3. Approve a User");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Log out");
            System.out.print("Choose an option (1-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> updateUser(admin);
                case "5" -> viewAuditLogs();
                case "6" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- All Registered Users ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- Pending Approvals ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending users found.");
        } else {
            printUserTable(pending);
        }
    }

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("There are no pending users to approve.");
            return;
        }

        System.out.println("\n--- Approve User ---");
        printUserTable(pending);

        System.out.print("\nEnter User ID to APPROVE (or enter 0 to cancel): ");
        int targetId = readIntInput();

        if (targetId == 0) {
            System.out.println("Action cancelled.");
            return;
        }

        boolean exists = pending.stream().anyMatch(u -> u.user_id() == targetId);

        if (exists) {
            if (userDao.approveUser(targetId)) {
                System.out.println("Success! User ID " + targetId + " has been APPROVED.");
            } else {
                System.out.println("Failed to update database.");
            }
        } else {
            System.out.println("Error: That User ID is not in the pending list.");
        }
    }

    private void updateUser(User currentAdmin) {
        System.out.println("\n--- Update User ---");
        printUserTable(userDao.getAllUsers());

        System.out.print("\nEnter User ID to update (or enter 0 to cancel): ");
        int targetId = readIntInput();

        if (targetId == 0) {
            System.out.println("Action cancelled.");
            return;
        }

        if (targetId == currentAdmin.user_id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean exists = userDao.getAllUsers().stream().anyMatch(u -> u.user_id() == targetId);
        if (!exists) {
            System.out.println("Error: User ID not found.");
            return;
        }

        String newRole = readValidRole();
        String newStatus = readValidStatus();

        if (userDao.updateUserRoleAndStatus(targetId, newRole, newStatus)) {
            System.out.println("Success! User ID " + targetId + " updated to Role: " + newRole + " | Status: " + newStatus);
        } else {
            System.out.println("Failed to update database.");
        }
    }

    private void viewAuditLogs() {
        System.out.println("\n--- System Audit Logs ---");
        System.out.println("1. View All Logs");
        System.out.println("2. Filter Logs by User ID");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        List<ActivityLog> logs;
        if (choice.equals("2")) {
            System.out.print("Enter User ID: ");
            int userId = readIntInput();
            logs = logDao.getLogsByUserId(userId);
        } else {
            logs = logDao.getAllLogs();
        }

        if (logs.isEmpty()) {
            System.out.println("No logs found.");
            return;
        }

        System.out.printf("\n%-5s | %-8s | %-18s | %-35s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (ActivityLog log : logs) {
            System.out.println(log.toString());
        }
    }

    private int readIntInput() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input (letters/symbols detected). Please enter a valid number: ");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role (DISPATCHER, WAREHOUSE MANAGER, BILLING STAFF, ADMIN): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("DISPATCHER") || input.equals("WAREHOUSE MANAGER") ||
                    input.equals("BILLING STAFF") || input.equals("ADMIN")) {
                return input;
            } else {
                System.out.println("Invalid role. Please try again.");
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (APPROVED, PENDING): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            } else {
                System.out.println("Invalid status. Please try again.");
            }
        }
    }

    private void printUserTable(List<User> users) {
        System.out.printf("%-5s %-20s %-18s %-10s%n", "ID", "Username", "Role", "Status");
        System.out.println("----------------------------------------------------------------------");
        for (User u : users) {
            System.out.printf("%-5d %-20s %-18s %-10s%n",
                    u.user_id(),
                    u.username(),
                    u.roles(),
                    u.status());
        }
        System.out.println("----------------------------------------------------------------------");
    }
}