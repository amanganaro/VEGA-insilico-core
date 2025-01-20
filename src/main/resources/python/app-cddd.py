import pandas as pd
import sys
import os
import argparse
from cddd.inference import InferenceModel
from cddd.preprocessing import preprocess_smiles

def main():
    parser = argparse.ArgumentParser(description='Continuous data driven descriptors calculation')
    parser.add_argument('--output', required=True, help='Output directory of files')
    parser.add_argument('--input', required=True, help='CSV smiles file input')
    args = parser.parse_args()
    file_location = args.input
    output_dir = args.output

    print("Start calculating cddd descriptors")
    input_df = pd.read_csv(file_location)
    input_df["smiles_preprocessed"] = input_df.smiles.map(preprocess_smiles)
    
    #####input_df = input_df.dropna()
    smiles_list = input_df["smiles_preprocessed"].tolist()
    filtered_smiles_list = input_df.dropna()["smiles_preprocessed"].tolist()
    non_nan_mask = ~pd.isnull(smiles_list)

    # Process the non-NaN smiles with the model
    inference_model = InferenceModel()
    filtered_smiles_embedding = inference_model.seq_to_emb(filtered_smiles_list)

    # Create the final embeddings list, filling NaN positions with None
    smiles_embedding = [[]] * len(smiles_list)
    j = 0
    for i, is_non_nan in enumerate(non_nan_mask):
        if is_non_nan:
            smiles_embedding[i] = filtered_smiles_embedding[j]
            j += 1

    embedding_df = pd.DataFrame(smiles_embedding,
                                columns=[f'cddd_{i+1}' for i in range(len(filtered_smiles_embedding[0]))])
    output_df = pd.concat([input_df.reset_index(drop=True), embedding_df], axis=1)

    for index, row in output_df.iterrows():
        file_name = f"{index}.csv"
        file_path = os.path.join(output_dir, file_name)
        row.to_frame().T.to_csv(file_path, index=False)

    print("Finish calculating cddd descriptors")

if __name__ == "__main__":
    main()