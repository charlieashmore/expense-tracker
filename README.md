# expense-tracker
A Java CLI application for tracking personal expenses, with CSV-based persistence

## ML Transaction Categoriser

Nationwide imports arrive without categories, and Monzo's categories differ from a consistent scheme. A machine-learning categoriser predicts a spending category from each transaction's description, giving one consistent categorisation across all sources.

### Approach

- **Training data:** transaction descriptions paired with categories, sourced from the Monzo export (As Monzo does provide categories with the data). Only spending transactions are used (matching what the app imports), and categories with too few examples are excluded as they cannot be learned reliably.
- **Text vectorisation:** descriptions are converted to numeric features using TF-IDF, which weights words by how informative they are — distinctive merchant names (e.g. "TESCO", "SPOTIFY") are weighted highly, while words common to every transaction (e.g. "GBR") are down-weighted meaning the model does not prioritise these words.
- **Model:** a Multinomial Naive Bayes classifier, a standard and effective baseline for text classification.

### Results

On an initial dataset of ~100 spending transactions across 6 categories, the model achieved **71% overall accuracy**. Performance varied by category in an interpretable way:

- **Strong:** categories with distinctive merchant names and sufficient examples (e.g. Eating Out, Entertainment) were predicted reliably.
- **Moderate:** the dominant category (Groceries) was caught consistently but somewhat over-predicted, as the model leans toward the majority class when uncertain (some General were predicted as Groceries due to uncertainty).
- **Weak:** sparse categories (Transport, Shopping — only a handful of examples each) and "General" performed poorly as the bulk of General transactions were people's names.

### Analysis and limitations

The model's weaknesses are primarily **data limitations rather than modelling flaws**:

- **Sparse categories** (Transport, Shopping) lacked enough examples to learn from. The clearest improvement is simply more training data from a longer time period.
- **The "General" category** largely consists of person-to-person payments, where the description is a person's name. A name carries no inherent category signal, so these are inherently difficult to predict from the description alone.

### Possible improvements at this stage

- Train on a larger dataset (longer export window) to improve sparse-category performance.
- Add a confidence threshold so low-certainty predictions are flagged as "Uncategorised" for manual review rather than guessed.