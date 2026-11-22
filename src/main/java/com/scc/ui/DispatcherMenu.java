package com.scc.ui;

import com.scc.dao.CustomerDao;
import com.scc.dao.InventoryDao;
import com.scc.dao.TransactionDao;
import com.scc.dao.UserDao;
import com.scc.model.Customer;
import com.scc.model.Inventory;
import com.scc.model.Transaction;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class DispatcherMenu extends BaseMenu {

    private final CustomerDao customerDao = new CustomerDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final TransactionDao transactionDao = new TransactionDao();

    public DispatcherMenu(UserDao userDao, Scanner scanner) {
        super(userDao, scanner);
    }

    public void display(User dispatcher) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n============================");
            System.out.println("      DISPATCHER MENU       ");
            System.out.println("============================");
            System.out.println("1. Register New Customer Account");
            System.out.println("2. Log New Delivery Transaction");
            System.out.println("3. View Customer Delivery History");
            System.out.println("4. Log out");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> registerNewCustomer(dispatcher);
                case "2" -> logDelivery(dispatcher.user_id());
                case "3" -> viewCustomerHistory();
                case "4" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void registerNewCustomer(User dispatcher) {
        System.out.println("\n--- REGISTER NEW CUSTOMER (Type 0 at any prompt to cancel) ---");


        String name = "";
        while (true) {
            System.out.print("Full Name: ");
            name = scanner.nextLine().trim();
            if (name.equals("0")) return;
            if (name.isEmpty()) {
                System.out.println("-> Error: Full Name cannot be empty.");
            } else {
                break;
            }
        }


        String address = "";
        while (true) {
            System.out.print("Address: ");
            address = scanner.nextLine().trim();
            if (address.equals("0")) return;
            if (address.isEmpty()) {
                System.out.println("-> Error: Address cannot be empty.");
            } else {
                break;
            }
        }


        String contact = "";
        while (true) {
            System.out.print("Contact Number : ");
            contact = scanner.nextLine().trim();
            if (contact.equals("0")) return;

            if (contact.isEmpty()) {
                System.out.println("-> Error: Contact Number cannot be empty.");
            } else if (!contact.matches("\\d+")) {
                System.out.println("-> Error: Invalid input. Please enter numbers only.");
            } else {
                break;
            }
        }

        String subType = "";
        while (true) {
            System.out.print("Select Subscription Type [1] WEEKLY | [2] MONTHLY: ");
            String subInput = scanner.nextLine().trim();

            if (subInput.equals("0")) return;

            switch (subInput) {
                case "1" -> {
                    subType = "WEEKLY";
                    break;
                }
                case "2" -> {
                    subType = "MONTHLY";
                    break;
                }
                default -> {
                    System.out.println("-> Error: Invalid selection. Please choose 1 or 2.");
                    continue; // Loop again
                }
            }
            break;
        }


        if (customerDao.addCustomer(dispatcher.user_id(), name, address, contact, subType)) {
            System.out.println("-> Success! Customer '" + name + "' has been registered.");
        } else {
            System.out.println("-> Failed to register customer.");
        }
    }

    private void logDelivery(int dispatcherId) {
        System.out.println("\n--- LOG NEW DELIVERY (Type 0 to cancel) ---");
        System.out.print("Enter Customer ID: ");
        int custId = readIntInput();
        if (custId == 0) return;

        Customer customer = customerDao.getCustomerById(custId);
        if (customer == null) {
            System.out.println("-> Error: Active customer not found.");
            return;
        }

        System.out.println("Customer Found: " + customer.full_name() + " | Balance: ₱" + customer.outstanding_balance());


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
                System.out.println("[" + (i + 1) + "] " + inv.container_type() + " (" + inv.quantity_full() + " in stock)");
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


        int qty = 0;
        while (true) {
            System.out.print("Enter quantity delivered: ");
            qty = readIntInput();
            if (qty > 0) break;
            System.out.println("-> Error: Quantity must be greater than 0.");
        }

        double amount = 0;
        while (true) {
            System.out.print("Enter total amount charged for this delivery: ₱");
            try {
                amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount > 0) break;
                System.out.println("-> Error: Amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("-> Error: Invalid amount format. Please enter numbers only.");
            }
        }


        if (inventoryDao.processDelivery(custId, dispatcherId, qty, selectedContainerType, amount)) {
            System.out.println("-> Successfully logged " + qty + " " + selectedContainerType + "(s) for " + customer.full_name() + ".");
        } else {
            System.out.println("-> Failed to log delivery. Please check the stock availability.");
        }
    }

    private void viewCustomerHistory() {
        System.out.println("\n+-------+----------------------+--------------+-----------------+");
        System.out.printf("| %-63s |%n", "VIEW CUSTOMER DELIVERY HISTORY");
        System.out.println("+-------+----------------------+--------------+-----------------+");

        List<Customer> customers = customerDao.getAllActiveCustomers();
        if (customers.isEmpty()) {
            System.out.println("| No active customers found in the system.                       |");
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

        System.out.print("\nEnter Customer ID to view full delivery history (or 0 to go back): ");
        int custId = readIntInput();

        if (custId == 0) return;

        Customer selectedCustomer = customers.stream()
                .filter(c -> c.customer_id() == custId)
                .findFirst()
                .orElse(null);

        if (selectedCustomer == null) {
            System.out.println("-> Error: Customer ID not found in the active list.");
            return;
        }

        System.out.println("\n--- TRANSACTION HISTORY FOR: " + selectedCustomer.full_name() + " ---");
        List<Transaction> history = transactionDao.getTransactionsByCustomerId(custId);

        if (history.isEmpty()) {
            System.out.println("No transactions found for this customer.");
            return;
        }

        System.out.println("+-------+--------------+----------+------------+-------------------------+");
        System.out.printf("| %-5s | %-12s | %-8s | %-10s | %-23s |%n", "ID", "Type", "Qty", "Amount", "Date");
        System.out.println("+-------+--------------+----------+------------+-------------------------+");

        for (Transaction t : history) {
            System.out.printf("| %-5d | %-12s | %-8d | ₱%-9.2f | %-23s |%n",
                    t.transaction_id(), t.transaction_type(), t.quantity(), t.amount_charged(), t.toString());
        }
        System.out.println("+-------+--------------+----------+------------+-------------------------+");
    }
}