import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Locale;

public class NationwideImporter extends CsvImporter {
    public static final DateTimeFormatter NATIONWIDE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Override
    protected int linesToSkip() {
        return 5;
    }

    @Override
    protected Expense parseExpense(String[] line) {
        if (line[3].isEmpty()) {
            return null;
        }
        LocalDate date = LocalDate.parse(line[0], NATIONWIDE_DATE_FORMATTER);
        String description = line[2].replaceAll("GB APPLEPAY \\d{4}", "").trim();
        double amount = Double.parseDouble(line[3].replace("£", "").replace(",", ""));
        String category = "Uncategorised";
        return new Expense(amount, category, description, date);
    }
}