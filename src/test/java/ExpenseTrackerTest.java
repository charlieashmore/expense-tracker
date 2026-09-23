import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;

public class ExpenseTrackerTest {

    private ExpenseTracker tracker;

    @BeforeEach
    public void setUp() {
        tracker = new ExpenseTracker();
    }

    @Test
    public void getTotalReturnsCorrectSum() {
        tracker.addExpense(new Expense(10.0, "Groceries", "Supermarket", LocalDate.now()));
        tracker.addExpense(new Expense(20.0, "Transport", "Bus ticket", LocalDate.now()));
        assertEquals(30.0, tracker.getTotal(), 0.001);
    }

    @Test
    public void getTotalReturnsZeroWhenEmpty() {
        assertEquals(0.0, tracker.getTotal(), 0.001);
    }

    @Test
    public void filterByCategoryReturnsMatchingExpenses() {
        tracker.addExpense(new Expense(10.0, "Groceries", "Supermarket", LocalDate.now()));
        tracker.addExpense(new Expense(20.0, "Entertainment", "Movie ticket", LocalDate.now()));
        List<Expense> filtered = tracker.filterByCategory("Groceries");
        assertEquals(1, filtered.size());
        assertEquals("Groceries", filtered.get(0).category());
    }

    @Test
    public void filterByCategoryReturnsEmptyWhenNoMatch() {
        tracker.addExpense(new Expense(10.0, "Groceries", "Supermarket", LocalDate.now()));
        List<Expense> filtered = tracker.filterByCategory("Transport");
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void filterByAmountReturnsExpensesAboveThreshold() {
        tracker.addExpense(new Expense(5.0, "Transport", "Train ticket", LocalDate.now()));
        tracker.addExpense(new Expense(15.0, "Entertainment", "Film ticket", LocalDate.now()));
        tracker.addExpense(new Expense(25.0, "Groceries", "Supermarket", LocalDate.now()));
        List<Expense> filtered = tracker.filterByAmount(10.0);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(expense -> expense.amount() >= 10.0));
    }

    @Test
    public void sortByAmountReturnsAscendingOrder() {
        tracker.addExpense(new Expense(25.0, "Groceries", "Supermarket", LocalDate.now()));
        tracker.addExpense(new Expense(5.0, "Transport", "Train ticket", LocalDate.now()));
        tracker.addExpense(new Expense(15.0, "Entertainment", "Film ticket", LocalDate.now()));
        List<Expense> sorted = tracker.sortByAmount();
        assertEquals(5.0, sorted.get(0).amount(), 0.001);
        assertEquals(15.0, sorted.get(1).amount(), 0.001);
        assertEquals(25.0, sorted.get(2).amount(), 0.001);
    }

    @Test
    public void updateCategoryChangesCategory() {
        tracker.addExpense(new Expense(10.0, "Uncategorised", "Boots", LocalDate.now()));
        tracker.updateCategory(0, "Groceries");
        assertEquals("Groceries", tracker.getExpenses().get(0).category());
    }

    @Test
    public void removeLastExpenseReducesSize() {
        tracker.addExpense(new Expense(10.0, "Groceries", "Supermarket", LocalDate.now()));
        tracker.addExpense(new Expense(5.0, "Transport", "Bus ticket", LocalDate.now()));
        tracker.removeLastExpense();
        assertEquals(1, tracker.getExpenses().size());
    }
}