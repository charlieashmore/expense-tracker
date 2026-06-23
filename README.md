# expense-tracker
A Java CLI application for tracking personal expenses, with CSV-based persistence

## ML Transaction Categoriser

Nationwide imports arrive without categories, and Monzo's categories differ from a consistent scheme. A machine-learning categoriser predicts a spending category from each transaction's description, giving one consistent categorisation across all sources.

### Approach

- **Training data:** transaction descriptions paired with categories, sourced from the Monzo export (As Monzo does provide categories with the data). Only spending transactions are used (matching what the app imports), non-spending categories (Income, Savings, Transfers) are excluded by name, and categories with too few examples are excluded as they cannot be learned reliably.
- **Text vectorisation:** descriptions are converted to numeric features using TF-IDF, which weights words by how informative they are — distinctive merchant names (e.g. "TESCO", "SPOTIFY") are weighted highly, while words common to every transaction (e.g. "GBR") are down-weighted, so the model does not prioritise them.
- **Model:** I compared two models, a Multinomial Naive Bayes (a standard baseline for text classification), and Logistic Regression.

### Evaluation method

Single train/test splits were found to give noisy accuracy figures that varied by several points between runs, because the available dataset is currently too small. To measure reliably, my evaluation uses **5-fold cross-validation**, which averages performance across multiple splits for a more trustworthy estimate.

### Results

On ~155 spending transactions across 6 categories, evaluated with 5-fold cross-validation:

| Model | Cross-validated accuracy |
|-------|--------------------------|
| Multinomial Naive Bayes | ~60% |
| Logistic Regression | ~73% |

These values are from an average across 5 folds giving a more concrete figure that actually means something instead of a lucky or unlucky single split figure. Logistic Regression performed notably better, likely because it does not assume words are independent (as Naive Bayes does), which was the main issue with the first model (e.g. Inconsistent naming conventions with TESCO STORES TESCO-STORES and TESCO PFS). It also handles class imbalance more gracefully.

Per-category performance (Multinomial Naive Bayes, single split):

```
               precision    recall  f1-score   support
   Eating out       1.00      0.60      0.75         5
Entertainment       1.00      0.60      0.75         5
      General       0.80      0.80      0.80         5
    Groceries       0.52      1.00      0.69        11
     Shopping       0.00      0.00      0.00         3
    Transport       0.00      0.00      0.00         3

     accuracy                           0.66        32
```

Both Shopping and Transport were unreliable here due to data sparsity, the model also over-predicted the Groceries Category - when uncertain, it defaulted to it as the dominant category.

Per-category performance (Logistic Regression, single split):

```
               precision    recall  f1-score   support
   Eating out       0.75      0.60      0.67         5
Entertainment       0.83      1.00      0.91         5
      General       0.80      0.80      0.80         5
    Groceries       0.73      1.00      0.85        11
     Shopping       1.00      0.67      0.80         3
    Transport       0.00      0.00      0.00         3

     accuracy                           0.78        32
```

Most categories are predicted reliably. Transport (only ~3 examples) is the main exception, reflecting data sparsity rather than a model limitation.

### Model Integration

Transactions are now automatically categorised during the import stage. It works by the Java application invoking the Python model using ProcessBuilder, giving transaction descriptions to the model and receiving predictions for the categories back. The model is trained once by running the categoriser.py file while in the virtual environment and saved, and then loaded for the prediction instead of retraining every import.

Due to data sparsity, there can be some uncertainty when the model predicts the category. Therefore, any prediction that falls under the confidence threshold of 0.5 will be left as "Uncategorised" instead of guessing. This is based on the idea that it is better to flag these uncertain transactions for manual review rather than guess them incorrectly.

The model successfully predicts categories for both Nationwide and Monzo importers even though the training data currently only includes Monzo transactions.

### Analysis and limitations

- **Model choice was the main improvement** - switching from Naive Bayes to Logistic Regression improved cross-validated accuracy by ~13 points, more than expanding the dataset did.
- **Remaining errors are largely data-driven** - Transport has too few examples to learn, which caps achievable accuracy.
- **Coverage limitation** - The model, by design, only categorises merchants that it has seen in training. It cannot generalise unfamiliar merchants (e.g. a supermarket currently not in the training data, or a one off transaction on an unfamiliar website).
- **More data alone didn't break the ceiling** - expanding the dataset left accuracy roughly unchanged until the model was switched, showing that model choice and data *quality* mattered more than data *quantity* here.
- **Monzo imports were categorised much better than Nationwide imports** - Monzo imports were categorised successfully at a rate of ~64%. Comparing this to the Nationwide rate of ~38% it was a lot more successful. This is due to the fact that the model is currently trained on solely Monzo transactions naturally giving a higher success rate. And the fact that both the Nationwide and Monzo accounts were largely used at different merchants. This exact scenario reflects a train/deploy distribution mismatch, meaning that the models success depends on the training data representing actual spending patterns.

### Possible improvements at this stage

- Train on a larger, more varied dataset to improve sparse-category performance.
- Train with both Monzo and Nationwide transactions instead of just Monzo.
- Add labelled Nationwide transactions to the training data so the model has exposure to both accounts.
- Manual category override feature to give the correct category to uncategorised transactions.