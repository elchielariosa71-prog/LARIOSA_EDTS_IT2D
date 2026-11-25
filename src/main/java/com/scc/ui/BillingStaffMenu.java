package com.scc.ui;

import com.scc.dao.CustomerDao;
import com.scc.dao.UserDao;
import com.scc.dao.ReportDao;
import com.scc.model.Customer;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class BillingStaffMenu extends BaseMenu {

    private final CustomerDao customerDao = new CustomerDao();
    private final ReportDao reportDao = new ReportDao();

    public BillingStaffMenu(UserDao userDao, Scanner scanner) {
        super(userDao, scanner);
    }

    public void display(User billingStaff) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n============================");
            System.out.println("      BILLING STAFF MENU    ");
            System.out.println("============================");
            System.out.println("1. View Customer Balances");
            System.out.println("2. Record Payment");
            System.out.println("3. Generate Monthly Report");
            System.out.println("4. Log out");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewBalances();
                case "2" -> recordPayment();
                case "3" -> generateReport();
                case "4" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewBalances() {
        System.out.println("\n+-------+----------------------+--------------+-----------------+");
        System.out.printf("| %-63s |%n", "CUSTOMER OUTSTANDING BALANCES");
        System.out.println("+-------+----------------------+--------------+-----------------+");

        List<Customer> customers = customerDao.getCustomersWithBalance();
        if (customers.isEmpty()) {
            System.out.println("| All customers are fully paid up!                               |");
            System.out.println("+-------+----------------------+--------------+-----------------+");
            return;
        }

        System.out.printf("| %-5s | %-20s | %-12s | %-15s |%n", "ID", "Full Name", "Subscription", "Balance");
        System.out.println("+-------+----------------------+--------------+-----------------+");

        for (Customer c : customers) {
            System.out.printf("| %-5d | %-20s | %-12s | ₱%-14.2f |%n",
                    c.customer_id(), c.full_name(), c.subscription_type(), c.outstanding_balance());
        }
        System.out.println("+-------+----------------------+--------------+-----------------+");
    }

    private void recordPayment() {
        System.out.println("\n--- RECORD PAYMENT ---");
        List<Customer> customers = customerDao.getCustomersWithBalance();
        if (customers.isEmpty()) {
            System.out.println("No customers have an outstanding balance.");
            return;
        }

        viewBalances();

        int custId = -1;
        while (true) {
            System.out.print("\nEnter Customer ID to record payment for (or 0 to cancel): ");
            custId = readIntInput();

            if (custId == 0) return;

            int finalCustId = custId;
            boolean exists = customers.stream().anyMatch(c -> c.customer_id() == finalCustId);
            if (exists) {
                break;
            } else {
                System.out.println("-> Error: Customer ID not found in the unpaid list.");
            }
        }

        Customer customer = customerDao.getCustomerById(custId);
        if (customer == null) {
            System.out.println("-> Error: Customer not found.");
            return;
        }

        double maxBalance = customer.outstanding_balance();
        System.out.println("\nCustomer: " + customer.full_name());
        System.out.println("Total Outstanding Balance: ₱" + String.format("%.2f", maxBalance));

        double amount = 0;
        while (true) {
            System.out.print("Enter amount paid (or 0 to cancel): ₱");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) return;


            try {
                amount = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("-> Error: Invalid input. Please enter numbers only.");
                continue;
            }


            if (amount <= 0) {
                System.out.println("-> Error: Payment amount must be greater than 0.");
                continue;
            }


            if (amount > maxBalance) {
                System.out.println("-> Error: Payment exceeds the outstanding balance of ₱" + String.format("%.2f", maxBalance));
                continue;
            }

            break;
        }


        if (customerDao.processPayment(custId, amount)) {
            double remaining = maxBalance - amount;


            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

            System.out.println("\n=============================================");
            System.out.println("               ECODELIVER TRACK              ");
            System.out.println("             OFFICIAL RECEIPT                ");
            System.out.println("=============================================");
            System.out.printf("Date: %-37s%n", formatter.format(now));
            System.out.printf("Customer: %-33s%n", customer.full_name());
            System.out.println("---------------------------------------------");
            System.out.printf("Previous Balance: %25s%n", "₱" + String.format("%.2f", maxBalance));
            System.out.printf("Amount Paid:      %25s%n", "₱" + String.format("%.2f", amount));
            System.out.println("---------------------------------------------");
            System.out.printf("REMAINING BALANCE:%25s%n", "₱" + String.format("%.2f", remaining));
            System.out.println("=============================================");
            System.out.println("      Thank you for your payment!            ");
            System.out.println("=============================================\n");

        } else {
            System.out.println("-> Failed to process payment in database.");
        }
    }

    private void generateReport() {
        System.out.println("\n--- GENERATE MONTHLY REPORT ---");


        java.time.LocalDate today = java.time.LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        System.out.println("Generating report for Current Month: " + today.getMonth() + " " + year + "...");

        reportDao.generateMonthlyReport(month, year);
    }
}