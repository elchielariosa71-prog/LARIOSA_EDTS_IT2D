package com.scc;


import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.AdminMenu;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();
    public static void main (String[] args){

        System.out.println("\n================================");
        System.out.println(" ECODELIVER TRACK");
        System.out.println("==================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch(choice){
            case "1":
                handlelogin();
                break;
            case "2":
                System.out.print("Enter new username: ");
                String user = sc.nextLine();
                System.out.print("Enter password: ");
                String pass = sc.nextLine();
                System.out.print("Enter full_name: ");
                String full_name = sc.nextLine();

                System.out.print("Select Role [1] WAREHOUSE MANAGER | [2] BILLING STAFF | [3] DISPATCHER | [4] ADMIN: ");
                int roleChoice = Integer.parseInt(sc.nextLine());
                String role;
                switch (roleChoice){
                    case 1:
                        role = "WAREHOUSE MANAGER";
                        break;
                    case 2:
                        role = "BILLING STAFF";
                        break;
                    case 3:
                        role = "DISPATCHER";
                        break;
                    case 4:
                        role = "ADMIN";
                        break;
                    default:
                        role = null;
                }

                if(role != null){
                    userDao.registerUser(user,pass,full_name,role);
                }else {

                    System.out.println("Invalid role selection");
                }
                break;

        }

    }private static void handlelogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        User user = userDao.login(username,password);
        if(user != null){
            System.out.println("\n -> Welcome " +user.full_name()+" | ROLE: "+user.roles()+"");
            switch (user.roles()){
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                default -> System.out.println("Account role not found!");
            }
        }
    }

}