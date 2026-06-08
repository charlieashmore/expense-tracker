import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Expense(double amount, String category, String description, LocalDate date) {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Override
    public String toString() {
        return String.format("£%8.2f %-15s %-20s %s", amount(), category(), description(), date().format(FORMATTER));
    }

    public String toCsv() {
        return amount() + "," + category() + "," + description() + "," + date().format(FORMATTER);
    }
}