import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class Categoriser {
    private static final String PYTHON_PATH = "ml/venv/Scripts/python.exe";
    private static final String SCRIPT_PATH = "predict.py";
    private static final String WORKING_DIR = "ml";
    private static final String INPUT_CSV = "to_categorise.csv";
    private static final String OUTPUT_CSV = "categorised.csv";

    public List<Expense> categoriseExpenses(List<Expense> expenses) {
        writeDescriptionsToCsv(expenses, WORKING_DIR + "/" + INPUT_CSV);
        boolean success = runPythonScript(INPUT_CSV, OUTPUT_CSV);
        if (!success) {
            System.out.println("Error running categorisation script.");
            return expenses;
        }
        List<String> categories = readCategories(WORKING_DIR + "/" + OUTPUT_CSV);
        List<Expense> categorisedExpenses = applyCategories(expenses, categories);
        return categorisedExpenses;
    }

    private void writeDescriptionsToCsv(List<Expense> expenses, String filename) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeNext(new String[]{"Description"});
            for (Expense expense : expenses) {
                writer.writeNext(new String[]{expense.description()});
            }
        }
        catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private boolean runPythonScript(String inputCsv, String outputCsv) {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_PATH, SCRIPT_PATH, inputCsv, outputCsv);
            pb.directory(new File(WORKING_DIR));
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            System.out.println("Error running categorisation: " + e.getMessage());
            return false;
        }
    }

    private List<String> readCategories(String filename) {
        List<String> categories = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            reader.skip(1);
            String[] line;
            while ((line = reader.readNext()) != null) {
                categories.add(line[1]);
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error reading from file: " + e.getMessage());
        }
        return categories;
    }

    private List<Expense> applyCategories(List<Expense> expenses, List<String> categories) {
        if (expenses.size() != categories.size()) {
            System.out.println("Mismatch between expenses and categories.");
            return expenses;
        }
        List<Expense> categorisedExpenses = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            Expense originalExpense = expenses.get(i);
            String category = categories.get(i);
            Expense categorisedExpense = new Expense(originalExpense.amount(), category, originalExpense.description(), originalExpense.date());
            categorisedExpenses.add(categorisedExpense);
        }
        return categorisedExpenses;
    }
}