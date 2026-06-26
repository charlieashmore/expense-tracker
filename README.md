# expense-tracker
A Java CLI application for tracking personal expenses with CSV-based persistence

## ML Transaction Categoriser

Nationwide imports arrive without categories, and Monzo's categories differ from a consistent scheme. A machine-learning categoriser predicts a spending category from each transaction's description, giving one consistent categorisation across all sources. The model is trained on data from both Nationwide and Monzo bank statements.

### Approach

- **Training data:** transaction descriptions paired with categories, sourced from the Monzo export (Monzo provides categories in its export), and manually labelled Nationwide data (based on the Monzo categories). Only spending transactions are used (matching what the app imports). Non-spending categories (Income, Savings, Transfers) are excluded by name, and categories with too few examples are excluded as they cannot be learned reliably.
- **Text vectorisation:** descriptions are converted to numeric features using TF-IDF, which weights words by how informative they are — distinctive merchant names (e.g. "TESCO", "SPOTIFY") are weighted highly, while words that appear in many transactions (e.g. "GBR") are down-weighted, so the model does not prioritise them.
- **Model:** I compared two models, a Multinomial Naive Bayes (a standard baseline for text classification), and Logistic Regression.

### Evaluation method

Single train/test splits were found to give noisy accuracy figures that varied by several points between runs, because the available dataset is currently too small. To measure reliably, my evaluation uses **5-fold cross-validation**, which averages performance across multiple splits for a more trustworthy estimate.

### Results

On ~155 spending transactions across 6 categories using Monzo data only, evaluated with 5-fold cross-validation:

| Model | Cross-validated accuracy |
|-------|--------------------------|
| Multinomial Naive Bayes | ~60% |
| Logistic Regression | ~73% |

On ~437 spending transactions across 6 categories using both Nationwide and Monzo data, evaluated with 5-fold cross-validation:

| Model | Cross-validated accuracy |
|-------|--------------------------|
| Logistic Regression | ~79% |

These values are averaged across five folds, giving a more reliable estimate than a single train/test split. Logistic Regression performed notably better, likely because it does not assume words are independent (as Naive Bayes does), which was the main issue with the first model (e.g. different naming conventions such as "TESCO STORES", "TESCO-STORES", and "TESCO PFS"). It also handles class imbalance more gracefully.

Although cross-validation is used to measure overall performance, the classification reports below show representative single train/test splits to illustrate category-level behaviour.

#### Monzo-only dataset

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

Both Shopping and Transport were unreliable here due to data sparsity. The model also over-predicted the Groceries category. When uncertain, it defaulted to it as the dominant category.

Per-category performance (Logistic Regression, single split) {Monzo-only}:

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

#### Combined dataset

Per-category performance (Logistic Regression, single split) {Nationwide and Monzo}:

```
               precision    recall  f1-score   support
   Eating out       0.78      0.82      0.80        17
Entertainment       0.67      0.40      0.50         5
      General       0.92      1.00      0.96        12
    Groceries       0.87      1.00      0.93        34
     Shopping       1.00      0.40      0.57         5
    Transport       1.00      0.87      0.93        15

     accuracy                           0.88        88
```

Although all categories are now predicted more accurately, the remaining low-frequency categories, particularly Shopping and Entertainment, would still benefit from additional training data.

### Model Integration

Transactions are now automatically categorised during the import stage. The Java application invokes the Python model using ProcessBuilder, passes transaction descriptions to it, and receives the predicted categories in return. The model is trained once by running the categoriser.py file while in the virtual environment, then saved to disk, and loaded for prediction instead of being retrained on every import.

Due to data sparsity, there can be some uncertainty when the model predicts the category. Therefore, any prediction that falls under the confidence threshold of 0.5 will be left as "Uncategorised" rather than forcing a prediction. This is based on the idea that it is better to flag these uncertain transactions for manual review rather than miscategorising them.

The model successfully predicts categories for both Nationwide and Monzo importers, having been trained on labelled data from both.

### Analysis and limitations

- **Model choice was the main improvement:** Switching from Naive Bayes to Logistic Regression improved cross-validated accuracy by ~13 percentage points, more than expanding the dataset did.
- **Coverage limitation:** Coverage depends on the training data representing real spending. The model performs best on merchants it has seen during training. The Monzo-only model handled Nationwide poorly (~62% uncategorised) because the cards are used at different merchants, training on both banks reduced this to ~21% uncategorised.
- **Remaining uncategorised transactions are mostly genuine unknowns:** One-off purchases and merchants that are not seen in training were rightfully left "Uncategorised" by using the confidence threshold.
- **Sparse categories remain the weak point of the model:** Categories such as Shopping and Entertainment are far less reliable than more data-rich categories such as Groceries. More data would help improve this.

### Possible improvements at this stage

- Collect more training data for the remaining unreliable categories (Shopping and Entertainment).
- Add a manual category override feature for uncategorised transactions.
- Periodically retrain the model as more labelled transactions accumulate.