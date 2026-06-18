import pandas as pd

import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.metrics import classification_report
from sklearn.model_selection import cross_val_score
from sklearn.pipeline import make_pipeline

from sklearn.linear_model import LogisticRegression


def explore_monzo_df(monzo_df):
    print(monzo_df.columns.tolist())
    print(monzo_df.shape)
    print(monzo_df[["Description", "Category", "Amount"]].head(10))
    print(monzo_df["Category"].value_counts())

def clean_monzo_df(monzo_df) -> tuple[pd.DataFrame, pd.Series, pd.Series]:
    spending_df = monzo_df[monzo_df["Amount"] < 0].copy()
    spending_df = spending_df.dropna(subset=["Description"])

    spending_df["Description"] = spending_df["Description"].str.replace(r"\s+", " ", regex=True).str.replace("-", " ").str.strip()

    non_spending_categories = ["Savings", "Investments", "Income", "Transfers"]
    spending_df = spending_df[~spending_df["Category"].isin(non_spending_categories)]
    category_counts = spending_df["Category"].value_counts()
    valid_categories = category_counts[category_counts >= 3].index
    spending_df = spending_df[spending_df["Category"].isin(valid_categories)]

    X = spending_df["Description"]
    y = spending_df["Category"]
    return spending_df[["Description", "Category", "Amount"]], X, y


def train_model(X, y) -> tuple[MultinomialNB, TfidfVectorizer, pd.DataFrame, pd.Series]:
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=42)

    vectoriser = TfidfVectorizer()
    X_train_vector = vectoriser.fit_transform(X_train)
    X_test_vector = vectoriser.transform(X_test)

    model = LogisticRegression(max_iter=1000)
    model.fit(X_train_vector, y_train)

    return model, vectoriser, X_test_vector, y_test

def evaluate_model(model, X_test_vector, y_test):
    y_predictions = model.predict(X_test_vector)
    print(classification_report(y_test, y_predictions, zero_division=0))

def cross_validate_model(X, y):
    model_pipeline = make_pipeline(TfidfVectorizer(), LogisticRegression(max_iter=1000))
    scores = cross_val_score(model_pipeline, X, y, cv=5)
    print(f"Cross-validation accuracy scores: {scores}")
    print(f"Average accuracy: {scores.mean()}")

def train_and_save_model(X, y, filename="model.pkl"):
    monzo_pipeline = make_pipeline(TfidfVectorizer(), LogisticRegression(max_iter=1000))
    monzo_pipeline.fit(X, y)
    joblib.dump(monzo_pipeline, filename)
    print(f"Model sucessfully saved to {filename}")

def main():
    monzo_df = pd.read_csv("../monzo_full.csv")
    explore_monzo_df(monzo_df)
    cleaned_df, X, y = clean_monzo_df(monzo_df)
    explore_monzo_df(cleaned_df)
    model, vectoriser, X_test_vector, y_test = train_model(X, y)
    evaluate_model(model, X_test_vector, y_test)
    cross_validate_model(X, y)
    train_and_save_model(X, y)

if __name__ == "__main__":
    main()