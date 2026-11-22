package com.scc.ui;

import com.scc.dao.UserDao;
import java.util.Scanner;

public abstract class BaseMenu {
    protected final UserDao userDao;
    protected final Scanner scanner;

    public BaseMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    protected int readIntInput() {
        while (true) {
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return 0;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number (or 0 to cancel): ");
            }
        }
    }


    protected boolean isCancelled(String input) {
        return input.equals("0");
    }
}