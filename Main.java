import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Main {
    private static final String DATA_FILE = "expenses.csv";

    public static void main(String[] args) {
        boolean running = true;
        ExpenseTracker et = new ExpenseTracker();
        Scanner scanner = new Scanner(System.in);

        et.loadFromCSVFile(DATA_FILE);

        while (running) {
            displayMenu();
            int choice = readInt(scanner, "");
            switch (choice) {
                case 1:
                    addExpenseFromInput(scanner, et);
                    et.saveToCSVFile(DATA_FILE);
                    break;
                case 2:
                    int sortChoice = readInt(scanner, "1 - Sort by amount (low to high)\n2 - Sort by amount (high to low)\n3 - Sort by date (oldest to newest)\n4 - Sort by date (newest to oldest)\n");
                    switch (sortChoice) {
                        case 1:
                            et.listSpecificExpenses(et.sortByAmount());
                            break;
                        case 2:
                            et.listSpecificExpenses(et.sortByAmountDescending());
                            break;
                        case 3:
                            et.listSpecificExpenses(et.sortByDate());
                            break;
                        case 4:
                            et.listSpecificExpenses(et.sortByDateDescending());
                            break;
                        default:
                            System.out.println("Invalid Option Selected. Returning to Menu.");
                            break;
                    }
                    break;
                case 3:
                    et.listAll();
                    break;
                case 4:
                    System.out.println("Enter category: ");
                    String category = scanner.nextLine();
                    et.listSpecificExpenses(et.filterByCategory(category));
                    break;
                case 5:
                    double minAmount = readDouble(scanner, "Enter minimum amount: ");
                    et.listSpecificExpenses(et.filterByAmount(minAmount));
                    break;
                case 6:
                    double total = et.getTotal();
                    System.out.println("Expense Total: £" + String.format("%.2f", total));
                    break;
                case 7:
                    displayBreakdown(et);
                    break;
                case 8:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid Option Selected.");
                    break;
            }
        }

        scanner.close();
        et.saveToCSVFile(DATA_FILE);
    }

    public static void displayMenu() {
        System.out.println("==== Expense Tracker ====\n1 - Add new expense\n2 - Show sorted expenses\n3 - Show all expenses\n4 - Show all expenses for a category\n5 - Show all expenses above a certain amount\n6 - Show total \n7 - Show category breakdown\n8 - Quit");
    }

    public static void addExpenseFromInput(Scanner scanner, ExpenseTracker et) {
        double amount = readDouble(scanner, "Enter amount: ");
        System.out.println("Expense category: ");
        String category = scanner.nextLine();
        System.out.println("Expense description: ");
        String description = scanner.nextLine();
        LocalDate date = readDate(scanner, "Enter date (dd/MM/yyyy): ");
        Expense expense = new Expense(amount, category, description, date);
        et.addExpense(expense);
    }

    public static void displayBreakdown(ExpenseTracker et) {
        Map<String, Double> breakdown = et.getCategoryBreakdown();
        breakdown.forEach((category, total) -> System.out.printf("%-15s £%.2f%n", category, total));
    }

    public static int readInt(Scanner scanner, String prompt) {
        int value = 0;
        while (true) {
            try {
                System.out.print(prompt);
                value = scanner.nextInt();
                scanner.nextLine();
                break;
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid whole number.");
                scanner.nextLine();
            }
        }
        return value;
    }

    public static double readDouble(Scanner scanner, String prompt) {
        double value = 0;
        while (true) {
            try {
                System.out.print(prompt);
                value = scanner.nextDouble();
                scanner.nextLine();
                break;
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                scanner.nextLine();
            }
        }
        return value;
    }

    public static LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.println(prompt);
                String inputString = scanner.nextLine();
                return LocalDate.parse(inputString, Expense.FORMATTER);
            }
            catch (DateTimeParseException e) {
                System.out.println("Please enter a valid date in the format dd/MM/yyyy.");
            }
        }
    }
}