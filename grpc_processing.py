import os
import re
from bs4 import BeautifulSoup
import pandas as pd

# 定义CSV文件路径和输出文件路径
csv_path = 'data'
power_path = "power_data"
output_file = 'average_results_grpc_v7.csv'
cpu_output_file = 'cpu_data_grpc_v7.csv'  # CPU数据输出文件

# 初始化两个空的DataFrame来存储结果
results = pd.DataFrame(columns=['File Name', 'Average Response Time', 'Request Type', 'Frequency', 'Size', 'Power'])
cpu_results = pd.DataFrame(columns=['File Name', 'CPU Data'])  # 存储CPU数据的DataFrame

# 遍历目录中的所有CSV文件
for filename in os.listdir(csv_path):
    if filename.endswith('.csv'):
        file_path = os.path.join(csv_path, filename)

        # 读取CSV文件
        df = pd.read_csv(file_path)

        # 计算各列的均值
        avg_response_time = df['Average Response Time'].mean()
        request_type_mean = df['Request Type'].mean()
        frequency_mean = df['Frequency'].mean()
        size_mean = df['Size'].mean()
        file_base_name = os.path.splitext(filename)[0]

        modified_file_name = re.sub(r'(\d+)', r'_rep\1', file_base_name)
        power_file = modified_file_name + '_powertop.html'
        power_file_path = os.path.join(power_path, power_file)

        # 初始化Power和CPU数据
        power_data = None
        cpu_data = None

        try:
            with open(power_file_path, 'r', encoding='utf-8') as file:
                html_content = file.read()

            soup = BeautifulSoup(html_content, 'html.parser')

            # 查找包含所需数据的<li>标签
            li_tags = soup.find_all('li', class_='summary_list')

            # 遍历所有找到的<li>标签，查找包含特定文本的标签
            for li in li_tags:
                b_tag = li.find('b')
                if b_tag and 'The system baseline power is estimated at:' in b_tag.text:
                    # 提取功率数据
                    power_data = li.text.split(':')[-1].strip().split()[0]
                    print(f"{power_file}: The system baseline power is estimated at: {power_data} W")

                if b_tag and 'CPU:' in b_tag.text:
                    # 提取CPU相关数据
                    cpu_data = li.text.split(':')[-1].strip()
                    print(f"{power_file}: CPU related data found: {cpu_data}")

        except FileNotFoundError:
            print(f"{power_file_path} not found, skipping this file.")

        # 创建一个包含均值的新DataFrame
        new_row = pd.DataFrame({
            'File Name': [file_base_name],
            'Average Response Time': [avg_response_time],
            'Request Type': [request_type_mean],
            'Frequency': [frequency_mean],
            'Size': [size_mean],
            'Power': [power_data]
        })

        # 将新行添加到结果DataFrame中
        results = pd.concat([results, new_row], ignore_index=True)

        # 创建包含CPU数据的新DataFrame行
        if cpu_data is not None:
            cpu_row = pd.DataFrame({
                'File Name': [file_base_name],
                'CPU Data': [cpu_data]
            })
            cpu_results = pd.concat([cpu_results, cpu_row], ignore_index=True)

# 将结果写入新的CSV文件
results.to_csv(output_file, index=False)
cpu_results.to_csv(cpu_output_file, index=False)  # 写入CPU数据的CSV文件

print(f'Results have been written to {output_file}')
print(f'CPU data has been written to {cpu_output_file}')
