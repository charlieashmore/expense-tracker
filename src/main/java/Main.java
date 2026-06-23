import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

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
                    et.removeLastExpense();
                    et.saveToCSVFile(DATA_FILE);
                    break;
                case 3:
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
                case 4:
                    et.listAll();
                    break;
                case 5:
                    System.out.println("Enter category: ");
                    String category = scanner.nextLine();
                    et.listSpecificExpenses(et.filterByCategory(category));
                    break;
                case 6:
                    double minAmount = readDouble(scanner, "Enter minimum amount: ");
                    et.listSpecificExpenses(et.filterByAmount(minAmount));
                    break;
                case 7:
                    double total = et.getTotal();
                    System.out.println("Expense Total: £" + String.format("%.2f", total));
                    break;
                case 8:
                    displayBreakdown(et);
                    break;
                case 9:
                    System.out.println("Nationwide or Monzo? (Enter 'N' for Nationwide, 'M' for Monzo): ");
                    String bank = scanner.nextLine();
                    System.out.println("Enter CSV filename: ");
                    String filename = scanner.nextLine();
                    if (bank.equalsIgnoreCase("N")) {
                        importFromBank(et, filename, new NationwideImporter());
                        et.saveToCSVFile(DATA_FILE);
                    } else if (bank.equalsIgnoreCase("M")) {
                        importFromBank(et, filename, new MonzoImporter());
                        et.saveToCSVFile(DATA_FILE);
                    } else {
                        System.out.println("Invalid option. Please enter 'N' for Nationwide or 'M' for Monzo.");
                    }
                    break;
                case 10:
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
        System.out.println("==== Expense Tracker ====\n1 - Add new expense\n2 - Remove last expense\n3 - Show sorted expenses\n4 - Show all expenses\n5 - Show all expenses for a category\n6 - Show all expenses above a certain amount\n7 - Show total \n8 - Show category breakdown\n9 - Import Bank CSV\n10 - Quit");
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

    public static void importFromBank(ExpenseTracker et, String filename, CsvImporter importer) {
        List<Expense> importedExpenses = importer.importFromCsv(filename);
        Categoriser categoriser = new Categoriser();
        List<Expense> categorisedExpenses = categoriser.categoriseExpenses(importedExpenses);
        et.addAll(categorisedExpenses);
        System.out.println("Imported " + categorisedExpenses.size() + " expenses.");
    }
}