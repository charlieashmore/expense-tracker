import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class MonzoImporter extends CsvImporter {
    public static final DateTimeFormatter MONZO_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected Expense parseExpense(String[] line) {
        LocalDate date = LocalDate.parse(line[1], MONZO_DATE_FORMATTER);
        String description = line[14].replaceAll("\\s+", " ").trim();
        double amount = Double.parseDouble(line[7].replace(",", ""));
        if (amount >= 0) {
            return null;
        }
        else {
            amount = -amount;
        }
        String category = "Uncategorised";
        return new Expense(amount, category, description, date);
    }
}
