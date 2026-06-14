import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public abstract class CsvImporter {

    public List<Expense> importFromCsv(String filename) {
        List<Expense> expenses = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            reader.skip(linesToSkip());
            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    Expense expense = parseExpense(line);
                    if (expense != null) {
                        expenses.add(expense);
                    }
                }
                catch (NumberFormatException | ArrayIndexOutOfBoundsException | DateTimeParseException e) {
                    System.out.println("Skipping malformed row: " + e.getMessage());
                }
            }
        }
        catch (IOException | CsvValidationException e) {
            System.out.println("Error reading from file: " + e.getMessage());
        }
        return expenses;
    }

    protected abstract Expense parseExpense(String[] line);

    protected int linesToSkip() {
        return 1;
    }
}