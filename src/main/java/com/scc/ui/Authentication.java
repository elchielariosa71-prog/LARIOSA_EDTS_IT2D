package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    // Passed from Main to prevent duplicate resource creation
    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n================================");
            System.out.println(" ECODELIVER TRACK");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "3" -> {
                    System.out.println("Exiting system. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void handleRegister() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter full_name: ");
        String fullName = scanner.nextLine();

        System.out.print("Select Role [1] WAREHOUSE MANAGER | [2] BILLING STAFF | [3] DISPATCHER | [4] ADMIN: ");
        String roleInput = scanner.nextLine();
        String role = switch (roleInput) {
            case "1" -> "WAREHOUSE MANAGER";
            case "2" -> "BILLING STAFF";
            case "3" -> "DISPATCHER";
            case "4" -> "ADMIN";
            default -> null;
        };

        if (role != null) {
            userDao.registerUser(username, password, fullName, role);
        } else {
            System.out.println("Invalid role selection.");
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User loggedInUser = userDao.login(username, password);

        if (loggedInUser != null) {
            System.out.println("\n -> Welcome " + loggedInUser.full_name() + " | ROLE: " + loggedInUser.roles());

            // ROUTING TO SPECIFIC ROLE MENUS
            switch (loggedInUser.roles()) {
                case "ADMIN" -> new AdminMenu(userDao, scanner).display(loggedInUser);
                // When you build the other menus, you add them here:
                // case "DISPATCHER" -> new DispatcherMenu(userDao, scanner).display(loggedInUser);
                // case "WAREHOUSE MANAGER" -> new WarehouseMenu(userDao, scanner).display(loggedInUser);
                default -> System.out.println("Account role not found or menu not yet implemented!");
            }
        }
    }
}