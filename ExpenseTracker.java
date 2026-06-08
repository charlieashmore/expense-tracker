import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.io.FileWriter;
import java.io.IOException;

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
        listSpecificExpenses(expenses);
    }

    public double getTotal() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.amount();
        }

        return total;
    }

    public void saveToCSVFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Expense expense : expenses) {
                writer.println(expense.toCsv());
            }
        }
        catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public void loadFromCSVFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    double amount = Double.parseDouble(parts[0]);
                    String category = parts[1];
                    String description = parts[2];
                    LocalDate date = LocalDate.parse(parts[3], Expense.FORMATTER);
                    Expense expense = new Expense(amount, category, description, date);
                    addExpense(expense);
                }
                catch (NumberFormatException | ArrayIndexOutOfBoundsException | DateTimeParseException e) {
                    System.out.println("Skipping malformed line in file: " + e.getMessage());
                }
            }
        }
        catch (FileNotFoundException e) {
            // no saved data yet — first run
        }
        catch (IOException e) {
            System.out.println("Error reading from file: " + e.getMessage());
        }
    }

    public List<Expense> filterByCategory(String category) {
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense.category().equals(category)) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }

    public void listSpecificExpenses(List<Expense> specificExpenses) {
        if (specificExpenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }
        for (Expense expense : specificExpenses) {
            System.out.println(expense);
        }
    }

    public List<Expense> filterByAmount(double amount) {
        return expenses.stream().filter(e -> e.amount() > amount).toList();
    }

    public double totalByCategory(String category) {
        List<Expense> filteredExpenses = filterByCategory(category);
        return filteredExpenses.stream().mapToDouble(e -> e.amount()).sum();
    }

    public Set<String> getDistinctCategories() {
        return expenses.stream().map(expense -> expense.category()).collect(Collectors.toSet());
    }

    public Map<String, Double> getCategoryBreakdown() {
        Map<String, Double> breakdown = new HashMap<>();
        for (String category : getDistinctCategories()) {
            breakdown.put(category, totalByCategory(category));
        }
        return breakdown;
    }

    public List<Expense> sortByAmount() {
        List<Expense> sortedExpenses = new ArrayList<>(expenses);
        sortedExpenses.sort(Comparator.comparingDouble(e -> e.amount()));
        return sortedExpenses;
    }

    public List<Expense> sortByAmountDescending() {
        List<Expense> sortedExpenses = new ArrayList<>(expenses);
        sortedExpenses.sort(Comparator.comparingDouble(Expense::amount).reversed());
        return sortedExpenses;
    }
}