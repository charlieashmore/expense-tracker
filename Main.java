import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean running = true;
        ExpenseTracker et = new ExpenseTracker();
        Scanner scanner = new Scanner(System.in);

        et.loadFromCSVFile("expenses.csv");

        while (running) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    addExpenseFromInput(scanner, et);
                    break;
                case 2:
                    et.listAll();
                    break;
                case 3:
                    double total = et.getTotal();
                    System.out.println("Expense Total: £" + String.format("%.2f", total));
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid Option Selected.");
                    break;
            }
        }

        scanner.close();
        et.saveToCSVFile("expenses.csv");
    }

    public static void displayMenu() {
        System.out.println("==== Expense Tracker ====");
        System.out.println("1 - Add new expense");
        System.out.println("2 - Show all expenses");
        System.out.println("3 - Show total ");
        System.out.println("4 - Quit");
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
        breakdown.forEach((category, total) -> System.out.printf("%-15s £%.2fn", category, total));
    }
}
