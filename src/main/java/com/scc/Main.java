package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Database
        DatabaseInitializer.initialize();

        // 2. Instantiate DAO & Utils (Single instances)
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        // 3. Delegate execution to Authentication
        Authentication auth = new Authentication(userDao, scanner);
        auth.start();
    }
}