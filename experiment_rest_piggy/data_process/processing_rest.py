import os
import re
from bs4 import BeautifulSoup
import pandas as pd

csv_path = 'data'
power_path = "power_data"
output_file = 'average_results_rest.csv'

results = pd.DataFrame(columns=['File Name', 'Average Response Time', 'Request Type', 'Frequency', 'Size', 'Power', 'CPU Data'])

for filename in os.listdir(csv_path):
    if filename.endswith('.csv'):
        file_path = os.path.join(csv_path, filename)

        df = pd.read_csv(file_path)

        avg_response_time = df['Average Response Time'].mean()
        request_type_mean = df['Request Type'].mean()
        frequency_mean = df['Frequency'].mean()
        size_mean = df['Size'].mean()
        file_base_name = os.path.splitext(filename)[0]

        modified_file_name = re.sub(r'(\d+)', r'_rep\1', file_base_name)
        power_file = modified_file_name + '_powertop.html'
        power_file_path = os.path.join(power_path, power_file)
        with open(power_file_path, 'r', encoding='utf-8') as file:
            html_content = file.read()

        soup = BeautifulSoup(html_content, 'html.parser')

        li_tags = soup.find_all('li', class_='summary_list')

        power_data = None
        cpu_data = None
        for li in li_tags:
            b_tag = li.find('b')
            if b_tag:
                if 'The system baseline power is estimated at:' in b_tag.text:
                    power_data = li.text.split(':')[-1].strip().split()[0]
                    print(f"{power_file}: The system baseline power is estimated at: {power_data} W")
                if 'CPU:' in b_tag.text:
                    cpu_data = li.text.split(':')[-1].strip()
                    print(f"{power_file}: CPU related data found: {cpu_data}")

        new_row = pd.DataFrame({
            'File Name': [file_base_name],
            'Average Response Time': [avg_response_time],
            'Request Type': [request_type_mean],
            'Frequency': [frequency_mean],
            'Size': [size_mean],
            'Power': [power_data],
            'CPU Data': [cpu_data]
        })

        results = pd.concat([results, new_row], ignore_index=True)

results.to_csv(output_file, index=False)

print(f'Results have been written to {output_file}')
