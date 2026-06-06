public record Expense(double amount, String category, String description, String date) {
    @Override
    public String toString() {
        return String.format("£%8.2f %-15s %-20s %s", amount(), category(), description(), date());
    }
}