import sys
import pandas as pd
import joblib

CONFIDENCE_THRESHOLD = 0.5

def predict_categories(input_filename, output_filename, model_file="model.pkl"):
    model = joblib.load(model_file)
    input_df = pd.read_csv(input_filename)
    descriptions = input_df["Description"].fillna("").str.replace(r"\s+", " ", regex=True).str.replace("-", " ").str.strip()

    probabilities = model.predict_proba(descriptions)
    predicted_categories = model.classes_[probabilities.argmax(axis=1)]
    max_confidence = probabilities.max(axis=1)

    categories = []
    for prediction, confidence in zip(predicted_categories, max_confidence):
        if confidence >= CONFIDENCE_THRESHOLD:
            category = prediction
        else:
            category = "Uncategorised"
        categories.append(category)

    result_df = pd.DataFrame({
        "Description": descriptions,
        "Category": categories
    })

    result_df.to_csv(output_filename, index=False)
    print(f"{len(categories)} predictions saved to {output_filename}")

def main():
    input_file = sys.argv[1] if len(sys.argv) > 1 else "input.csv"
    output_file = sys.argv[2] if len(sys.argv) > 2 else "output.csv"
    predict_categories(input_file, output_file)

if __name__ == "__main__":
    main()