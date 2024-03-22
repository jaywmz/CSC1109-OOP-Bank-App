import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BankUI {
    private static Scanner scanner = new Scanner(System.in);
    private static Map<String, Insurance> insurancePolicies = new HashMap<>();

    public static void printMainMenu(Bank bank) {
        System.out.println("------------------------------------");
        System.out.println("Welcome to " + bank.getBankName() + "! Please give us your money!");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.println("4. Branches");
        System.out.println("------------------------------------");
        System.out.print("Enter your choice: ");
    }

    public static void displayMainMenu(Bank bank) {
        while (true) {
            printMainMenu(bank);
            int choice = BankUI.getUserChoice();

            switch (choice) {
                case 1:
                    BankUI.register(bank);
                    break;
                case 2:
                    BankUI.login(bank);
                    break;
                case 3:
                    System.exit(0);
                    break;
                case 4:
                    BankUI.viewBranches(bank);
                    break;
                default:
                    BankUI.printInvalid();
                    break;
            }
        }
    }

    public static void register(Bank bank) {
        try {
            while (true) {
                System.out.println("------------------------------------");
                System.out.print("Enter username: ");
                String username = scanner.nextLine();

                // validate username entered, check if username already exists
                String customerString = CSVHandler.getRecord(username, "CustomerInfo.csv");
                if (customerString != null) {
                    System.out.println("Username already exists. Please try again.");
                    continue;
                }

                System.out.print("Enter 6 digit PIN: ");
                String password = scanner.nextLine();

                // validate PIN entered, first check if is numerical, then check if length is 6 digits
                int pin;
                try {
                    pin = Integer.parseInt(password);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid password entered. Please try again.");
                    continue;
                }
                if (password.length() != 6) {
                    System.out.println("Invalid password entered. Please try again.");
                    continue;
                }
                else {
                    password = PasswordHasher.hashPassword(String.valueOf(pin));
                }

                System.out.print("Enter ID: ");
                String id = scanner.nextLine();

                Customer.registerCustomer(username, password, "User", id);
                System.out.println("Registration successful!");
                break;
            }
        } catch (Exception e) {
            System.out.println("An error occurred during registration: " + e.getMessage());
        }
    }

    public static void login(Bank bank) {
        try {
            System.out.println("------------------------------------");
            System.out.print("Enter username: ");
            String loginUsername = scanner.nextLine();
            System.out.print("Enter password: ");
            String loginPassword = scanner.nextLine();

            Customer customer = CSVHandler.retrieveCustomer(loginUsername);
            if (customer != null && customer.isLocked()) {
                System.out.println("The account is locked and cannot be accessed.");
                return;
            }

            if (Customer.loginCustomer(loginUsername, loginPassword)) {
                // Retrieve the full customer details after successful authentication
                if ("Admin".equals(customer.getRole())) {
                    adminMenu(bank, customer);
                } else {
                    accountsMenu(bank, customer);
                }
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred during login: " + e.getMessage());
        }
    }

    public static void printAdminMenu(Customer admin) {
        System.out.println("------------------------------------");
        System.out.println("Welcome to the admin menu, " + admin.getUsername() + "!");
        System.out.println("1. View all customers");
        System.out.println("2. Add a new customer");
        System.out.println("3. Remove a customer");
        System.out.println("4. Unlock a customer account");
        System.out.println("5. Logout");
        System.out.println("------------------------------------");
        System.out.print("Enter your choice: ");
    }

    public static void adminMenu(Bank bank, Customer admin) {
        while (true) {
            printAdminMenu(admin);
            int choice = BankUI.getUserChoice();

            switch (choice) {
                case 1:
                    // Implement the logic to view all customers
                    Customer.viewAllCustomers();
                    break;
                case 2:
                    Customer.addNewCustomer();
                    break;
                case 3:
                    // Implement the logic to remove a customer
                    Customer.removeCustomer();
                    break;
                case 4:
                    // unlock customer account given the customer's username
                    Customer.unlockCustomerAccount();
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.");
                    break;
            }
        }
    }

    public static void accountsMenu(Bank bank, Customer customer) {
        if (customer.isLocked()) {
            System.out.println("The account is locked and cannot be accessed.");
            return;
        }
        
        while (true) {
            String custAccountsRecord = CSVHandler.getRecord(customer.getUsername(), "CustomerAccounts.csv");
            String[] accounts = custAccountsRecord.split(",");
            String choice = "";

            System.out.println("------------------------------------");
            System.out.println("Welcome " + customer.getUsername() + "!");
            System.out.println("Which account would you like to view?");
            // accounts[0] contains username, so to iterate through account numbers, start index at 1
            int i = 1;
            for (; i < accounts.length; i++) {
                System.out.println(i + ". " + accounts[i]);
            }
            System.out.println(i + ". Make new bank account");
            System.err.println((i+1) + ". Logout");
            System.out.println("------------------------------------");
            System.out.print("Enter your choice: ");
            
            try {
                choice = scanner.nextLine();
                if (Integer.parseInt(choice) == (i+1)) { // i + 1 corresponds to logout option
                    return;
                }
                else if (Integer.parseInt(choice) == i) { // i corresponds to make new bank account option
                    // call generate random acount num
                    String randomAccNum = Bank.generateAccNum();
                    // create new account obj, call account addRecord()
                    Account newAccount = new Account(randomAccNum);
                    CSVHandler.addRecord("Accounts.csv", newAccount.convertToCSV());
                    // update CustomerAccounts.csv with new account added to customer account info
                    String newCustomerRecord = custAccountsRecord + "," + randomAccNum;
                    CSVHandler.updateCSV(customer.getUsername(), "CustomerAccounts.csv", newCustomerRecord);
                    System.out.println("New bank account created successfully.");
                }
                else {
                    // go through all accounts and see if user choice entered corresponds with any existing accounts
                    for (int j = 1; j < accounts.length; j++) {
                        if (Integer.parseInt(choice) == j) {
                            transactMenu(bank, customer, accounts[j]);
                            continue;
                        }
                    }
                    // if the option number the user entered does not correspond to an existing account number, then print below
                    BankUI.printInvalid();
                }
            } catch (InputMismatchException | NumberFormatException e) {
                BankUI.printInvalid();
                continue;
            }
        }
    }

    public static void displayAccountMenu(Account loggedInAccount) {
        System.out.println("------------------------------------");
        System.out.println("Account number: " + loggedInAccount.getAccountNum());
        System.out.println("Balance: " + Account.convert2DP(loggedInAccount.getBalance()));
        System.out.println("1. Transfer Funds");
        System.out.println("2. Change transfer limit");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Currency Exchange");
        System.out.println("6. Get a loan");
        System.out.println("7. Pay loan");
        System.out.println("8. Create Insurance Policy");
        System.out.println("9. Go back to accounts menu");
        System.out.println("------------------------------------");
        System.out.print("Enter your choice: ");
    }

    public static void performTransfer(Bank bank, Account loggedInAccount) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the account number to transfer money to: ");
        String transferAccountNum = scanner.next();
        System.out.print("Enter the amount to transfer: $");
        double transferAmount = scanner.nextDouble();
        scanner.nextLine();     // Consumes the \n after the double
        bank.transferMoney(loggedInAccount.getAccountNum(), transferAccountNum, transferAmount);
    }

    public static void updateTransferLimit(Account loggedInAccount) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("------------------------------------");
            System.out.println("Current transfer limit: $" + Account.convert2DP(loggedInAccount.getTransLimit()));
            System.out.print("Enter new transfer limit: $");
            try {
                String newTransLimitStr = scanner.nextLine();
                double newTransLimit = Double.parseDouble(newTransLimitStr);
                loggedInAccount.setTransferLimit(newTransLimit);
                CSVHandler.updateCSV(loggedInAccount.getAccountNum(), "Accounts.csv", loggedInAccount.convertToCSV());
                System.out.println("Transfer limit changed successfully!");
                break;
            } catch (InputMismatchException | NumberFormatException e) {
                BankUI.printInvalid();
            }
        }
    }
    
    public static void performDeposit(Account loggedInAccount) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the amount to deposit: $");
        double depositAmount = scanner.nextDouble();
        scanner.nextLine();     // Consumes the \n after the double
        loggedInAccount.deposit(depositAmount);
    }

    public static void performWithdrawal(Account loggedInAccount) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the amount to withdraw: $");
        double withdrawAmount = scanner.nextDouble();
        scanner.nextLine();     // Consumes the \n after the double
        loggedInAccount.withdraw(withdrawAmount);
    }

    public static void performCurrencyExchange(Account loggedInAccount) {
        Scanner scanner = new Scanner(System.in);
        ForeignExchange foreignExchange = new ForeignExchange();
        foreignExchange.displayRates();
        System.out.println("Enter the currency to convert from (SGD/USD/JPY):");
        String fromCurrency = scanner.next();
        double exchangeAmount;
        if (fromCurrency.equals("SGD")){
            System.out.println("Enter the amount to exchange: ");
            exchangeAmount = scanner.nextDouble();
            scanner.nextLine();     // Consumes the \n after the double
            if(loggedInAccount.getBalance() < exchangeAmount){
                System.out.println("Not enough money in balance!");
                return;
            }
            loggedInAccount.setBalance(loggedInAccount.getBalance() - exchangeAmount);
        }else{
            System.out.println("Please insert foreign cash into the machine");
            System.out.println("Amount: ");
            exchangeAmount = scanner.nextDouble();
            scanner.nextLine();     // Consumes the \n after the double
        }
        System.out.println("Enter the currency to convert to (SGD/USD/JPY):");
        String toCurrency = scanner.next();

        double convertedAmount = foreignExchange.convert(fromCurrency, toCurrency, exchangeAmount);
        if(toCurrency.equals("SGD")){
            System.out.println("Converted amount: " + convertedAmount + " SGD");
            System.out.println("Adding to balance...");
            loggedInAccount.setBalance(loggedInAccount.getBalance() + convertedAmount);
        }else{
            System.out.println("Converted amount: " + convertedAmount + " " + toCurrency);
            System.out.println("Dispensing amount...");
        }
    }

    public static void createLoan(Scanner scanner) {
        try {
            System.out.print("Loan amount: ");
            float principal = Float.parseFloat(scanner.nextLine());
            System.out.print("Loan term (1 to 7 years): ");
            int loanTermMonths = Integer.parseInt(scanner.nextLine()) * 12;
            LocalDate date = LocalDate.now();
            // hard coded annual flat rate of 6.0%
            double interestRate = 0.06;
            // G16_LON loan = new G16_LON(principal, interestRate, date, loanTermMonths);
            // loan.displayLoanDetails();
        } catch (NumberFormatException e) {
            BankUI.printInvalid();
        }
    }

    public static void transactMenu(Bank bank, Customer customer, String accNum) {
        while (true) {
            Account loggedInAccount = new Account(accNum);
            displayAccountMenu(loggedInAccount);
            int choice = BankUI.getUserChoice();

            switch (choice) {
                case 1:
                    // Transfer money
                    performTransfer(bank, loggedInAccount);
                    break;
                case 2:
                    // Change transfer limit
                    updateTransferLimit(loggedInAccount);
                    break;
                case 3:
                    // Deposit
                    performDeposit(loggedInAccount);
                    break;
                case 4:
                    // Withdraw
                    performWithdrawal(loggedInAccount);
                    break;
                case 5:
                    // Foreign Exchange
                    performCurrencyExchange(loggedInAccount);
                    break;
                case 6:
                    // get a loan (implement 506)
                    // have not implemented saving loan info to file and linking it to account
                    createLoan(scanner);
                    break;
                case 7:
                    // paying back a loan (implement 506/507)
                    break;
                case 8:
                    createNewInsurancePolicy();
                    break;
                case 9:
                    // go back to accounts menu
                    return;
                default:
                    printInvalid();
                    break;
            }
        }
    }
    //create policy
    public static void createNewInsurancePolicy() {
        while (true) {
            try {
                System.out.println("Enter policy type (1 for LIFE, 2 for HEALTH, 3 for ACCIDENT): ");
                int policyTypeIndex = scanner.nextInt();
                if (policyTypeIndex < 1 || policyTypeIndex > 3) {
                    throw new InputMismatchException();
                }

                System.out.println("Enter coverage option (1 for BASIC($1000), 2 for STANDARD($2000), 3 for PREMIUM($3000)): ");
                int coverageOptionIndex = scanner.nextInt();
                if (coverageOptionIndex < 1 || coverageOptionIndex > 3) {
                    throw new InputMismatchException();
                }

                System.out.println("Enter policy tenure (1 for FIVE_YEARS, 2 for TEN_YEARS, 3 for FIFTEEN_YEARS, 4 for TWENTY_YEARS): ");
                int policyTenureIndex = scanner.nextInt();
                if (policyTenureIndex < 1 || policyTenureIndex > 4) {
                    throw new InputMismatchException();
                }

                System.out.println("Enter premium frequency (1 for MONTHLY, 2 for QUARTERLY, 3 for SEMI_ANNUALLY, 4 for ANNUALLY): ");
                int premiumFrequencyIndex = scanner.nextInt();
                if (premiumFrequencyIndex < 1 || premiumFrequencyIndex > 4) {
                    throw new InputMismatchException();
                }

                System.out.println("Enter policy start date (yyyy-MM-dd): ");
                String startDateString = scanner.next();
                LocalDate.parse(startDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd")); // This will throw DateTimeParseException if the date is not valid

                scanner.nextLine(); // Consume the newline character

                Insurance insurance = new Insurance(policyTypeIndex, startDateString, coverageOptionIndex, policyTenureIndex, premiumFrequencyIndex);
                // Display the policy details and print them
                System.out.println(insurance.displayPolicyDetails());

                System.out.println("Create policy successfully!");
                System.out.println("------------------------------------");
                break; // Break the loop if everything is valid
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
                scanner.nextLine(); // Consume the invalid input
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter the date in the format yyyy-MM-dd.");
                scanner.nextLine(); // Consume the invalid input
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again.");
                scanner.nextLine(); // Consume the invalid input
            }
        }
    }
    
    public static void viewBranches(Bank bank) {
        ArrayList<Branch> branches = new ArrayList<Branch>();
        try (BufferedReader br = new BufferedReader(new FileReader("Branches.csv"))){
            String branchInfo;
            while ((branchInfo = br.readLine()) != null) {
                String attributes[] = branchInfo.split(",");
                Branch branch = new Branch(Integer.parseInt(attributes[0]), attributes[1], attributes[2], LocalTime.parse(attributes[3]), LocalTime.parse(attributes[4]));
                branches.add(branch);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(e);
            return;
        }
        while (true) {
            System.out.println("------------------------------------");
            System.out.println("These are our branches!");
            for (Branch branch : branches) {
                System.out.println("--------------");
                System.out.println("Branch name: " + branch.getBranchName());
                System.out.println("Address: " + branch.getBranchAddress());
                System.out.println("Opening time: " + branch.getOpeningTime());
                System.out.println("Closing time: " + branch.getClosingTime());
            }
            System.out.println("--------------");
            System.out.print("Press enter to return to main menu. ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                return;
            }
        }
    }

    public static void printInvalid() {
        System.out.println("------------------------------------");
        System.out.println("Invalid input. Please try again.");
    }
   
    public static int getUserChoice() {
        try {
            String choice = scanner.nextLine();
            // scanner.nextLine();     // Consumes the \n after the integer
            return Integer.parseInt(choice);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    // ... more methods for other menus and user input
}