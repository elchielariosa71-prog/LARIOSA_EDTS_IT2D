package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;


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
        System.out.println("\n--- REGISTRATION (Type 0 at any prompt to cancel) ---");

        String username = "";
        while (true) {
            System.out.print("Enter new username: ");
            username = scanner.nextLine().trim();
            if (username.equals("0")) {
                System.out.println("-> Registration cancelled.");
                return;
            }
            if (userDao.isUsernameTaken(username)) {
                System.out.println("-> Error: Username '" + username + "' is already taken. Please try another one.");

            } else {
                break;
            }
        }


        String password = "";
        while (true) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (password.equals("0")) {
                System.out.println("-> Registration cancelled.");
                return;
            }
            if (password.isEmpty()) {
                System.out.println("-> Error: Password cannot be empty.");
            } else {
                break;
            }
        }


        String fullName = "";
        while (true) {
            System.out.print("Enter full name (First Name and Last Name): ");
            fullName = scanner.nextLine().trim();
            if (fullName.equals("0")) {
                System.out.println("-> Registration cancelled.");
                return;
            }


            if (!fullName.contains(" ") || fullName.split(" ").length < 2) {
                System.out.println("-> Error: Please enter both your First Name and Last Name.");
            } else {
                break;
            }
        }


        String role = "";
        while (true) {
            System.out.print("Select Role [1] WAREHOUSE MANAGER | [2] BILLING STAFF | [3] DISPATCHER | [4] ADMIN | [0] Cancel: ");
            String roleInput = scanner.nextLine().trim();

            if (roleInput.equals("0")) {
                System.out.println("-> Registration cancelled.");
                return;
            }


            if (roleInput.equals("4") && userDao.getAdminCount() > 0) {
                System.out.println("-> Error: An Admin account already exists. You cannot register a second Admin.");
                continue;
            }

            role = switch (roleInput) {
                case "1" -> "WAREHOUSE MANAGER";
                case "2" -> "BILLING STAFF";
                case "3" -> "DISPATCHER";
                case "4" -> "ADMIN";
                default -> null;
            };

            if (role != null) {
                break;
            } else {
                System.out.println("-> Invalid role selection. Please enter 1, 2, 3, or 4.");
            }
        }


        userDao.registerUser(username, password, fullName, role);
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User loggedInUser = userDao.login(username, password);

        if (loggedInUser != null) {
            new com.scc.dao.ActivityLogDao().logAction(loggedInUser.user_id(), "LOGIN", "User '" + username + "' logged into the system.");

            System.out.println("\n -> Welcome " + loggedInUser.full_name() + " | ROLE: " + loggedInUser.roles());

            switch (loggedInUser.roles()) {
                case "ADMIN" -> new AdminMenu(userDao, scanner).display(loggedInUser);
                case "WAREHOUSE MANAGER" -> new WarehouseManagerMenu(userDao, scanner).display(loggedInUser);
                case "BILLING STAFF" -> new BillingStaffMenu(userDao, scanner).display(loggedInUser);
                case "DISPATCHER" -> new DispatcherMenu(userDao, scanner).display(loggedInUser);
                default -> System.out.println("Account role not found!");
            }
        }
    }
}