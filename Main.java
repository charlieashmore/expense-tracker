public class Main {
    public static void main(String[] args) {

    ExpenseTracker et = new ExpenseTracker();
    Expense e1 = new Expense(25.00, "Transport", "Train Ticket", "05/06/2026");
    Expense e2 = new Expense(4.00, "Food", "Meal Deal", "05/06/2026");
    Expense e3 = new Expense(60.50, "Shopping", "New Clothes", "03/06/26");

    et.addExpense(e1);
    et.addExpense(e2);
    et.addExpense(e3);
    et.listAll();
    double currTotal = et.getTotal();

    System.out.println("Total: £" + String.format("%.2f", currTotal));
    }
}
