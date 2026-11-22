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
        System.out.println("+-----------------------------------------------------------------------+");
        System.out.printf("| %-69s |%n", "ALL REGISTERED USERS");
        System.out.println("+-----------------------------------------------------------------------+");

        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n+-----------------------------------------------------------------------+");
        System.out.printf("| %-69s |%n", "PENDING APPROVALS");
        System.out.println("+-----------------------------------------------------------------------+");

        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("| No pending users found.                                               |");
            System.out.println("+-----------------------------------------------------------------------+");
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


        User targetUser = userDao.getAllUsers().stream()
                .filter(u -> u.user_id() == targetId)
                .findFirst()
                .orElse(null);

        if (targetUser == null) {
            System.out.println("Error: User ID not found.");
            return;
        }

        System.out.println("\nWhat would you like to update?");
        System.out.println("1. Update Role (DISPATCHER, WAREHOUSE MANAGER, BILLING STAFF)");
        System.out.println("2. Update Status (ACTIVE, INACTIVE)");
        System.out.print("Choose an option (1 or 2): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            String newRole = readValidRole();


            if (targetUser.roles().equals(newRole)) {
                System.out.println("-> Notice: User " + targetId + " is already assigned to the " + newRole + " role.");
                return;
            }

            if (userDao.updateUserRole(targetId, newRole)) {
                System.out.println("Success! User ID " + targetId + " updated to Role: " + newRole);
            } else {
                System.out.println("Failed to update database.");
            }

        } else if (choice.equals("2")) {
            String newStatus = readValidStatus();


            if (targetUser.status().equalsIgnoreCase(newStatus)) {
                System.out.println("-> Notice: User " + targetId + " is ALREADY " + newStatus + ". No changes made.");
                return;
            }

            if (userDao.updateUserStatus(targetId, newStatus)) {
                System.out.println("Success! User ID " + targetId + " updated to Status: " + newStatus);
            } else {
                System.out.println("Failed to update database.");
            }
        } else {
            System.out.println("-> Invalid choice.");
        }
    }

    private void viewAuditLogs() {
        boolean viewing = true;
        while (viewing) {
            System.out.println("\n+-----------------------------------------------------------+");
            System.out.printf("| %-57s |%n", "SYSTEM AUDIT LOGS");
            System.out.println("+-----------------------------------------------------------+");
            System.out.println("| 1. View All Logs                                          |");
            System.out.println("| 2. Filter Logs by User ID                                 |");
            System.out.println("| 0. Back to Admin Menu                                     |");
            System.out.println("+-----------------------------------------------------------+");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                viewing = false;
                continue;
            }

            List<ActivityLog> logs;

            if (choice.equals("2")) {
                System.out.print("Enter User ID: ");
                int userId = readIntInput();
                logs = logDao.getLogsByUserId(userId);
            } else if (choice.equals("1")) {
                logs = logDao.getAllLogs();
            } else {
                System.out.println("Invalid choice.");
                continue;
            }

            if (logs.isEmpty()) {
                System.out.println("\nNo logs found.");
            } else {

                System.out.println("\n+-------+---------+------------------+---------------------------------------+------------------+");
                System.out.printf("| %-5s | %-7s | %-16s | %-37s | %-16s |%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
                System.out.println("+-------+---------+------------------+---------------------------------------+------------------+");

                for (ActivityLog log : logs) {

                    String desc = log.description();
                    if (desc.length() > 37) {
                        desc = desc.substring(0, 34) + "...";
                    }


                    String timeStr = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a").format(log.timestamp());

                    System.out.printf("| %-5d | %-7d | %-16s | %-37s | %-16s |%n",
                            log.id(),
                            log.userId(),
                            log.actionType(),
                            desc,
                            timeStr);
                }
                System.out.println("+-------+---------+------------------+---------------------------------------+------------------+");
            }
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
            System.out.print("Enter new Role (DISPATCHER, WAREHOUSE MANAGER, BILLING STAFF): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("ADMIN")) {
                System.out.println("-> Error: Cannot assign ADMIN role. There can only be one Admin.");
                continue;
            }

            if (input.equals("DISPATCHER") || input.equals("WAREHOUSE MANAGER") || input.equals("BILLING STAFF")) {
                return input;
            } else {
                System.out.println("Invalid role. Please try again.");
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (ACTIVE, INACTIVE): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("ACTIVE") || input.equals("INACTIVE")) {
                return input;
            } else {
                System.out.println("Invalid status. Please try again.");
            }
        }
    }

    private void printUserTable(List<User> users) {
        System.out.println("+-------+----------------------+----------------------+--------------+");
        System.out.printf("| %-5s | %-20s | %-20s | %-12s |%n", "ID", "Username", "Role", "Status");
        System.out.println("+-------+----------------------+----------------------+--------------+");

        for (User u : users) {
            System.out.printf("| %-5d | %-20s | %-20s | %-12s |%n",
                    u.user_id(),
                    u.username(),
                    u.roles(),
                    u.status());
        }
        System.out.println("+-------+----------------------+----------------------+--------------+");
    }
}