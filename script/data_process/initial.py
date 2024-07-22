import csv
import os

# Define file types with their corresponding frequency and size
file_type_info = {
    "low_small_grpc": ("low", "small"),
    "medium_small_grpc": ("medium", "small"),
    "high_small_grpc": ("high", "small"),
    "low_medium_grpc": ("low", "medium"),
    "medium_medium_grpc": ("medium", "medium"),
    "high_medium_grpc": ("high", "medium"),
    "low_large_grpc": ("low", "large"),
    "medium_large_grpc": ("medium", "large"),
    "high_large_grpc": ("high", "large"),
}

# Mapping for frequency and size
frequency_mapping = {"low": 0, "medium": 1, "high": 2}
size_mapping = {"small": 0, "medium": 1, "large": 2}
request_type_mapping = {"grpc": 1, "rest": 0}  # Assuming 'grpc' as 1 and 'rest' as 0 for request type

# Input directory
input_dir = 'row_data/'

# Output directory
output_dir = 'data/'

def process_file(input_file, output_file, frequency, size):
    try:
        # Open input file for reading
        with open(input_file, 'r') as infile:
            reader = csv.reader(infile)

            # Open output file for writing
            with open(output_file, 'w', newline='') as outfile:
                writer = csv.writer(outfile)

                # Write header
                writer.writerow(['Service',
                                 'Average Response Time',
                                 'Medium Response Time',
                                 'Request Size',
                                 'Request Type',
                                 'Frequency',
                                 'Size'])

                # Skip the first row (header of the input file)
                next(reader, None)

                for row in reader:
                    # Skip rows that do not contain data (e.g., the final aggregated row)
                    if len(row) < 3:
                        continue

                    service = row[1]
                    average_response_time = row[5]
                    medium_response_time = row[4]
                    request_size = row[8]
                    request_type = row[0]

                    # Validate numeric fields
                    try:
                        average_response_time = float(average_response_time)
                        medium_response_time = float(medium_response_time)
                        request_size = float(request_size)
                    except ValueError:
                        continue

                    # Convert request_type from string to number
                    request_type_num = request_type_mapping.get(request_type, -1)  # Default to -1 if unknown

                    # Write processed row
                    writer.writerow([service, average_response_time, medium_response_time, request_size, request_type_num, frequency, size])

        print(f"File '{input_file}' processed. Results saved in '{output_file}'.")

    except FileNotFoundError:
        print(f"Error: The file '{input_file}' was not found.")
    except IOError:
        print(f"Error: An I/O error occurred while accessing the file '{input_file}' or '{output_file}'.")
    except Exception as e:
        print(f"An unexpected error occurred while processing '{input_file}': {e}")

def clean_and_combine_files(output_dir, combined_file):
    first_file = True
    header_written = False

    # Create the combined file
    with open(combined_file, 'w', newline='') as outfile:
        writer = csv.writer(outfile)

        for file_name in os.listdir(output_dir):
            if file_name.endswith('.csv') and file_name != 'combined_data.csv':
                file_path = os.path.join(output_dir, file_name)

                with open(file_path, 'r') as infile:
                    reader = csv.reader(infile)
                    rows = list(reader)

                    if not rows:
                        continue

                    # Remove the last row
                    rows = rows[:-1]

                    # Remove the first column, third column, and fourth column
                    cleaned_rows = [[row[i] for i in range(len(row)) if i not in [0, 2, 3]] for row in rows]

                    if first_file:
                        # Write header for the combined file
                        writer.writerow(cleaned_rows[0])  # Write header row
                        header_written = True
                        first_file = False

                    # Write data rows
                    writer.writerows(cleaned_rows[1:])  # Write data without header

    print(f"All files have been combined into '{combined_file}'.")

# Process files for each file type
for file_type, (frequency_str, size_str) in file_type_info.items():
    frequency = frequency_mapping.get(frequency_str, -1)  # Default to -1 if unknown
    size = size_mapping.get(size_str, -1)  # Default to -1 if unknown

    for i in range(1, 1):  # Assuming there are 10 files per type
        input_file = os.path.join(input_dir, f'{file_type}{i}_stats.csv')
        output_file = os.path.join(output_dir, f'{file_type}{i}.csv')

        # Process the file
        process_file(input_file, output_file, frequency, size)

# Combine files
combined_file = 'combined_data.csv'
clean_and_combine_files(output_dir, combined_file)

print("All files have been processed and combined.")
