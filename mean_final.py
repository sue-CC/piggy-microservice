import pandas as pd
import re

# Load the file
file_path = 'average_results_grpc_v7.csv'
data = pd.read_csv(file_path)

# Extract the variable combination (excluding the numerical suffix)
def extract_combination(file_name):
    return re.sub(r'\d+$', '', file_name)

# Apply the extraction function to create a new column for grouping
data['Combination'] = data['File Name'].apply(extract_combination)

# Group by the new 'Combination' column and calculate the mean for other columns
grouped_data_with_prefix = data.groupby('Combination').mean(numeric_only=True).reset_index()

# Rename 'Combination' column to 'File Name Prefix'
grouped_data_with_prefix = grouped_data_with_prefix.rename(columns={'Combination': 'File Name Prefix'})

# Save the result to a new CSV file
output_file_path = 'average_results_mean_v7.csv'
grouped_data_with_prefix.to_csv(output_file_path, index=False)

# Display the path to the new CSV file
print(output_file_path)
