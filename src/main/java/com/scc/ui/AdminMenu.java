package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

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
            System.out.println("4. Log out");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> {
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
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println("Action cancelled.");
            return;
        }

        try {
            int targetId = Integer.parseInt(input);

            // Check if the ID actually exists in the pending list
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
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    // Helper method to format the table exactly as requested
    private void printUserTable(List<User> users) {
        // Format: %-5s (ID), %-15s (Username), %-12s (Role), %-10s (Status)
        System.out.printf("%-5s %-15s %-12s %-10s%n", "ID", "Username", "Role", "Status");
        System.out.println("--------------------------------------------------");

        for (User u : users) {
            System.out.printf("%-5d %-15s %-12s %-10s%n",
                    u.user_id(),
                    u.username(),
                    u.roles(),
                    u.status());
        }
        System.out.println("--------------------------------------------------");
    }
}