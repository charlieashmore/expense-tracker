import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "expenses.csv";

    public static void main(String[] args) {
        boolean running = true;
        ExpenseTracker et = new ExpenseTracker();
        Scanner scanner = new Scanner(System.in);

        et.loadFromCSVFile(DATA_FILE);

        while (running) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    addExpenseFromInput(scanner, et);
                    et.saveToCSVFile(DATA_FILE);
                    break;
                case 2:
                    System.out.println("1 - Ascending (lowest expense first)");
                    System.out.println("2 - Descending (highest expense first)");
                    int sortChoice = scanner.nextInt();
                    scanner.nextLine();
                    switch (sortChoice) {
                        case 1:
                            et.listSpecificExpenses(et.sortByAmount());
                            break;
                        case 2:
                            et.listSpecificExpenses(et.sortByAmountDescending());
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
                    double minAmount = 0;
                    while (true) {
                        try {
                            System.out.println("Enter mininum amount: ");
                            minAmount = scanner.nextDouble();
                            scanner.nextLine();
                            break;
                        }
                        catch (InputMismatchException e) {
                            System.out.println("Not a expense amount value.");
                            scanner.nextLine();
                        }
                    }
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
        System.out.println("==== Expense Tracker ====");
        System.out.println("1 - Add new expense");
        System.out.println("2 - Show sorted expenses");
        System.out.println("3 - Show all expenses");
        System.out.println("4 - Show all expenses for a category");
        System.out.println("5 - Show all expenses above a certain amount");
        System.out.println("6 - Show total ");
        System.out.println("7 - Show category breakdown");
        System.out.println("8 - Quit");
    }

    public static void addExpenseFromInput(Scanner scanner, ExpenseTracker et) {
        double amount = 0;

        while (true) {
            try {
                System.out.println("Expense amount: ");
                amount = scanner.nextDouble();
                scanner.nextLine();
                break;
            }
            catch (InputMismatchException e) {
                System.out.println("Not a expense amount value.");
                scanner.nextLine();
            }
        }

        System.out.println("Expense category: ");
        String category = scanner.nextLine();
        System.out.println("Expense description: ");
        String description = scanner.nextLine();
        System.out.println("Expense date: ");
        String date = scanner.nextLine();

        Expense expense = new Expense(amount, category, description, date);
        et.addExpense(expense);
    }

    public static void displayBreakdown(ExpenseTracker et) {
        Map<String, Double> breakdown = et.getCategoryBreakdown();
        breakdown.forEach((category, total) -> System.out.printf("%-15s £%.2f%n", category, total));
    }
}