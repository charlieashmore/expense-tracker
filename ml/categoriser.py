import pandas as pd

import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.metrics import classification_report
from sklearn.model_selection import cross_val_score
from sklearn.pipeline import make_pipeline

from sklearn.linear_model import LogisticRegression

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

def clean_nationwide_df(nationwide_df) -> tuple[pd.DataFrame, pd.Series, pd.Series]:
    spending_df = nationwide_df[nationwide_df["Paid in"].isna()].copy()
    spending_df = spending_df.dropna(subset=["Description", "Category"])

    spending_df["Description"] = (spending_df["Description"].str.replace(r" GB APPLEPAY \d{4}", "", regex=True).str.replace(r"\s+", " ", regex=True).str.replace("-", " ").str.strip())

    non_spending_categories = ["Savings", "Investments", "Income", "Transfers"]
    spending_df = spending_df[~spending_df["Category"].isin(non_spending_categories)]
    category_counts = spending_df["Category"].value_counts()
    valid_categories = category_counts[category_counts >= 3].index
    spending_df = spending_df[spending_df["Category"].isin(valid_categories)]

    X = spending_df["Description"]
    y = spending_df["Category"]
    return spending_df[["Description", "Category", "Paid out"]], X, y

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
    model_pipeline = make_pipeline(TfidfVectorizer(), LogisticRegression(max_iter=1000))
    model_pipeline.fit(X, y)
    joblib.dump(model_pipeline, filename)
    print(f"Model sucessfully saved to {filename}")

def combine_training_data(monzo_X, monzo_y, nationwide_X, nationwide_y):
    X = pd.concat([monzo_X, nationwide_X], ignore_index=True)
    y = pd.concat([monzo_y, nationwide_y], ignore_index=True)
    return X, y

def main():
    monzo_df = pd.read_csv("../monzo_full.csv")
    nationwide_df = pd.read_csv("../nationwide_with_cat.csv")
    cleaned_df_monzo, X_m, y_m = clean_monzo_df(monzo_df)
    cleaned_df_nationwide, X_n, y_n = clean_nationwide_df(nationwide_df)
    X, y = combine_training_data(X_m, y_m, X_n, y_n)
    model, vectoriser, X_test_vector, y_test = train_model(X, y)
    evaluate_model(model, X_test_vector, y_test)
    cross_validate_model(X, y)
    train_and_save_model(X, y)

if __name__ == "__main__":
    main()