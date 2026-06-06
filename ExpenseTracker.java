import java.util.ArrayList;
import java.util.List;

public class ExpenseTracker {
    private List<Expense> expenses = new ArrayList<>();

    public ExpenseTracker() {

    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public void removeExpense(Expense expense) {
        expenses.remove(expense);
    }

    public void listAll() {
        if (expenses.isEmpty()) {
            System.out.println("No Expenses Recorded.");
            return;
        }
        for (Expense expense : expenses) {
            System.out.println(expense);
        }
    }

    public double getTotal() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.amount();
        }

        return total;
    }
}