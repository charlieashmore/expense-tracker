import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;


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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            for (Expense expense : expenses) {
                String[] row = {String.valueOf(expense.amount()), expense.category(), expense.description(), expense.date().format(Expense.FORMATTER)};
                writer.writeNext(row);
            }
        }
        catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public void loadFromCSVFile(String filename) {
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    double amount = Double.parseDouble(line[0]);
                    String category = line[1];
                    String description = line[2];
                    LocalDate date = LocalDate.parse(line[3], Expense.FORMATTER);
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
        catch (IOException | CsvValidationException e) {
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
        return filteredExpenses.stream().mapToDouble(Expense::amount).sum();
    }

    public Set<String> getDistinctCategories() {
        return expenses.stream().map(Expense::category).collect(Collectors.toSet());
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
        sortedExpenses.sort(Comparator.comparingDouble(Expense::amount));
        return sortedExpenses;
    }

    public List<Expense> sortByAmountDescending() {
        List<Expense> sortedExpenses = new ArrayList<>(expenses);
        sortedExpenses.sort(Comparator.comparingDouble(Expense::amount).reversed());
        return sortedExpenses;
    }

    public List<Expense> sortByDate() {
        List<Expense> sortedExpenses = new ArrayList<>(expenses);
        sortedExpenses.sort(Comparator.comparing(Expense::date));
        return sortedExpenses;
    }

    public List<Expense> sortByDateDescending() {
        List<Expense> sortedExpenses = new ArrayList<>(expenses);
        sortedExpenses.sort(Comparator.comparing(Expense::date).reversed());
        return sortedExpenses;
    }

    public void removeLastExpense() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to remove.");
            return;
        }
        Expense removedExpense = expenses.remove(expenses.size() - 1);
        System.out.println("Removed last expense: " + removedExpense);
    }

    public void addAll(List<Expense> newExpenses) {
        expenses.addAll(newExpenses);
    }
}