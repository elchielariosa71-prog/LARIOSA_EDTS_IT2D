package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
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
            System.out.println("2. Log out");
            System.out.print("Choose an option (1-2): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n--- All Registered Users ---");
                    for (User u : userDao.getAllUsers()) {
                        System.out.println(u.toString());
                    }
                    break;
                case "2":
                    System.out.println("Logging out...");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}