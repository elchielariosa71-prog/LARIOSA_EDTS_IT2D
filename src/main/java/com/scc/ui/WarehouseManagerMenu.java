package com.scc.ui;

import com.scc.dao.CustomerDao;
import com.scc.dao.InventoryDao;
import com.scc.dao.UserDao;
import com.scc.model.Customer;
import com.scc.model.Inventory;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class WarehouseManagerMenu extends BaseMenu {

    private final CustomerDao customerDao = new CustomerDao();
    private final InventoryDao inventoryDao = new InventoryDao();

    public WarehouseManagerMenu(UserDao userDao, Scanner scanner) {
        super(userDao, scanner);
    }

    public void display(User manager) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n============================");
            System.out.println("   WAREHOUSE MANAGER MENU   ");
            System.out.println("============================");
            System.out.println("1. View Inventory Dashboard");
            System.out.println("2. Process Container Return");
            System.out.println("3. Update Customer Account Details");
            System.out.println("4. View All Customers");
            System.out.println("5. Log out");
            System.out.print("Choose an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewInventory();
                case "2" -> processReturn(manager.user_id());
                case "3" -> updateCustomer();
                case "4" -> viewAllCustomers();
                case "5" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewInventory() {
        System.out.println("\n--- REAL-TIME INVENTORY DASHBOARD ---");
        List<Inventory> stock = inventoryDao.getDashboard();
        if (stock.isEmpty()) {
            System.out.println("Inventory is empty. Please seed data.");
            return;
        }


        int totalContainersOut = 0;
        List<Customer> customers = customerDao.getAllActiveCustomers();
        for (Customer c : customers) {
            totalContainersOut += customerDao.getContainersOut(c.customer_id());
        }


        System.out.println("+----------------------+-------------+-------------+-------------+-------------+");
        System.out.printf("| %-20s | %-11s | %-11s | %-11s | %-11s |%n",
                "Container Type", "Full (Ready)", "Empty", "With Cust.", "Total Owned");
        System.out.println("+----------------------+-------------+-------------+-------------+-------------+");

        int grandTotalFull = 0;
        int grandTotalEmpty = 0;

        for (Inventory inv : stock) {
            int totalOwned = inv.quantity_full() + inv.quantity_empty() + totalContainersOut;

            System.out.printf("| %-20s | %-11d | %-11d | %-11d | %-11d |%n",
                    inv.container_type(), inv.quantity_full(), inv.quantity_empty(), totalContainersOut, totalOwned);

            grandTotalFull += inv.quantity_full();
            grandTotalEmpty += inv.quantity_empty();
        }

        System.out.println("+----------------------+-------------+-------------+-------------+-------------+");
        System.out.printf("| %-20s | %-11d | %-11d | %-11d | %-11d |%n",
                "TOTALS", grandTotalFull, grandTotalEmpty, totalContainersOut, (grandTotalFull + grandTotalEmpty + totalContainersOut));
        System.out.println("+----------------------+-------------+-------------+-------------+-------------+");
    }

    private void processReturn(int managerId) {
        System.out.println("\n--- PROCESS CONTAINER RETURN ---");
        List<Customer> customers = customerDao.getAllActiveCustomers();
        if (customers.isEmpty()) {
            System.out.println("No active customers found.");
            return;
        }


        System.out.println("+-------+----------------------+--------------+-----------------+");
        System.out.printf("| %-5s | %-20s | %-12s | %-15s |%n", "ID", "Full Name", "Containers Out", "Balance");
        System.out.println("+-------+----------------------+--------------+-----------------+");

        for (Customer c : customers) {
            int containersOut = customerDao.getContainersOut(c.customer_id());
            System.out.printf("| %-5d | %-20s | %-12d | ₱%-14.2f |%n",
                    c.customer_id(), c.full_name(), containersOut, c.outstanding_balance());
        }
        System.out.println("+-------+----------------------+--------------+-----------------+");


        System.out.print("\nEnter Customer ID to process return (or 0 to cancel): ");
        int custId = readIntInput();
        if (custId == 0) return;

        boolean exists = customers.stream().anyMatch(c -> c.customer_id() == custId);
        if (!exists) {
            System.out.println("-> Error: Customer ID not found.");
            return;
        }


        List<Inventory> stockList = inventoryDao.getDashboard();
        if (stockList.isEmpty()) {
            System.out.println("-> Error: No inventory found in the system.");
            return;
        }

        String selectedContainerType = "";
        while (true) {
            System.out.println("\nAvailable Container Types:");
            for (int i = 0; i < stockList.size(); i++) {
                Inventory inv = stockList.get(i);
                System.out.println("[" + (i + 1) + "] " + inv.container_type());
            }

            System.out.print("Select Container Type (or 0 to cancel): ");
            String containerInput = scanner.nextLine().trim();

            if (containerInput.equals("0")) return;

            try {
                int index = Integer.parseInt(containerInput) - 1;
                if (index >= 0 && index < stockList.size()) {
                    selectedContainerType = stockList.get(index).container_type();
                    break;
                } else {
                    System.out.println("-> Error: Invalid number. Please choose a number from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("-> Error: Please enter a valid number.");
            }
        }


        System.out.print("Enter quantity returned: ");
        int qty = readIntInput();
        if (qty <= 0) {
            System.out.println("-> Error: Quantity must be greater than 0.");
            return;
        }


        if (inventoryDao.processReturn(custId, managerId, qty, selectedContainerType)) {
            System.out.println("-> Successfully processed return of " + qty + " " + selectedContainerType + "(s).");
        } else {
            System.out.println("-> Failed to process return.");
        }
    }

    private void updateCustomer() {
        System.out.println("\n--- UPDATE CUSTOMER ACCOUNT DETAILS ---");
        List<Customer> customers = customerDao.getAllActiveCustomers();
        if (customers.isEmpty()) {
            System.out.println("No active customers found.");
            return;
        }


        System.out.println("+-------+----------------------+-----------------+------------+");
        System.out.printf("| %-5s | %-20s | %-15s | %-10s |%n", "ID", "Full Name", "Subscription", "Balance");
        System.out.println("+-------+----------------------+-----------------+------------+");

        for (Customer c : customers) {
            System.out.printf("| %-5d | %-20s | %-15s | ₱%-9.2f |%n",
                    c.customer_id(), c.full_name(), c.subscription_type(), c.outstanding_balance());
        }
        System.out.println("+-------+----------------------+-----------------+------------+");

        System.out.print("\nEnter Customer ID to update (or 0 to cancel): ");
        int custId = readIntInput();
        if (custId == 0) return;

        Customer target = customers.stream().filter(c -> c.customer_id() == custId).findFirst().orElse(null);
        if (target == null) {
            System.out.println("-> Error: Customer ID not found.");
            return;
        }

        System.out.println("\nCurrent Address: " + target.address());
        System.out.print("Enter new Address: ");
        String newAddress = scanner.nextLine().trim();
        if (newAddress.isEmpty()) newAddress = target.address();

        System.out.print("Enter new Subscription Type [1] WEEKLY | [2] MONTHLY: ");
        String subInput = scanner.nextLine().trim();
        String newSub = target.subscription_type();
        if (subInput.equals("1")) newSub = "WEEKLY";
        else if (subInput.equals("2")) newSub = "MONTHLY";

        if (customerDao.updateCustomerDetails(custId, newAddress, newSub)) {
            System.out.println("-> Success! Customer details updated.");
        } else {
            System.out.println("-> Failed to update customer.");
        }
    }

    private void viewAllCustomers() {
        System.out.println("\n--- ALL REGISTERED CUSTOMERS ---");
        List<Customer> customers = customerDao.getAllActiveCustomers();
        if (customers.isEmpty()) {
            System.out.println("No active customers found.");
            return;
        }


        System.out.println("+-------+----------------------+--------------+-----------------+-----------------+------------+");
        System.out.printf("| %-5s | %-20s | %-12s | %-15s | %-15s | %-10s |%n",
                "ID", "Full Name", "Subscription", "Contact", "Containers Out", "Balance");
        System.out.println("+-------+----------------------+--------------+-----------------+-----------------+------------+");

        for (Customer c : customers) {
            int containersOut = customerDao.getContainersOut(c.customer_id());
            System.out.printf("| %-5d | %-20s | %-12s | %-15s | %-15d | ₱%-9.2f |%n",
                    c.customer_id(), c.full_name(), c.subscription_type(), c.contact_number(), containersOut, c.outstanding_balance());
        }
        System.out.println("+-------+----------------------+--------------+-----------------+-----------------+------------+");
    }
}