import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# 设置文件路径
file_path = '/Users/suecai/Downloads/piggy-microservice/script/data_process/data/'  # 根据实际文件路径调整

# 获取文件列表
files = sorted([file for file in os.listdir(file_path) if file.endswith('.csv')])

# 读取文件并按组进行处理
data = {}
overall_data = {'Frequency': [], 'Request Type': [], 'Size': [], 'Average Response Time': []}
for file in files:
    group = file.split('_grpc')[1].split('.')[0]
    category = file.split('_')[0]
    if group not in data:
        data[group] = {}
    df = pd.read_csv(os.path.join(file_path, file))
    data[group][category] = df

    # 将数据添加到 overall_data 中
    overall_data['Frequency'].append(df['Frequency'])
    overall_data['Request Type'].append(df['Request Type'])
    overall_data['Size'].append(df['Size'])
    overall_data['Average Response Time'].append(df['Average Response Time'])

# 合并 overall_data 中的数据
for key in overall_data:
    overall_data[key] = pd.concat(overall_data[key], ignore_index=True)

# 进行频率、类型、大小和响应时间的统计分析
analysis_results = {}
for group, categories in data.items():
    analysis_results[group] = {}
    for cat, df in categories.items():
        analysis_results[group][cat] = {
            'mean_frequency': df['Frequency'].mean(),
            'mean_type': df['Request Type'].mean(),
            'mean_size': df['Size'].mean(),
            'mean_response_time': df['Average Response Time'].mean()
        }

# 设置颜色调色板
palette = sns.color_palette("Set2", 3)  # 设置为3种颜色

# 绘制频率的分布图
plt.figure(figsize=(12, 6))
sns.histplot(overall_data['Frequency'], kde=True, color='blue')
plt.xlabel('Frequency')
plt.ylabel('Count')
plt.title('Overall Frequency Distribution')
plt.show()

# 绘制类型的分布图（如果类型是分类变量）
plt.figure(figsize=(12, 6))
sns.countplot(x=overall_data['Request Type'])
plt.xlabel('Request Type')
plt.ylabel('Count')
plt.title('Overall Type Distribution')
plt.show()

# 绘制大小的分布图
plt.figure(figsize=(12, 6))
sns.histplot(overall_data['Size'], kde=True, color='green')
plt.xlabel('Size')
plt.ylabel('Count')
plt.title('Overall Size Distribution')
plt.show()

# 绘制响应时间的分布图
plt.figure(figsize=(12, 6))
sns.histplot(overall_data['Average Response Time'], kde=True, color='red')
plt.xlabel('Average Response Time')
plt.ylabel('Count')
plt.title('Overall Average Response Time Distribution')
plt.show()

# 绘制每组和类别的响应时间箱线图
for group, categories in data.items():
    response_times = pd.concat([df['Average Response Time'] for df in categories.values()], ignore_index=True)
    labels = []
    for category in categories:
        labels.extend([category] * len(categories[category]['Average Response Time']))

    plt.figure(figsize=(12, 6))
    sns.boxplot(x=labels, y=response_times, palette=palette)
    plt.xlabel('Category')
    plt.ylabel('Response Time')
    plt.title(f'Group {group} Response Time Distribution')
    plt.show()

# 绘制每组的平均值数据
mean_values = []
group_labels = []
categories_labels = []

for group, categories in data.items():
    categories_names = list(categories.keys())
    means = [df['Average Response Time'].mean() for df in categories.values()]
    for category, mean in zip(categories_names, means):
        mean_values.append(mean)
        group_labels.append(f'Group {group}')
        categories_labels.append(category)

# 将平均值、标签和类别放入数据框中
mean_df = pd.DataFrame({
    'Group': group_labels,
    'Category': categories_labels,
    'Mean_Response_Time': mean_values
})

# 绘制箱线图
plt.figure(figsize=(12, 6))
sns.boxplot(x='Group', y='Mean_Response_Time', hue='Category', data=mean_df, palette='Set2')
plt.xlabel('Group')
plt.ylabel('Mean Response Time')
plt.title('Distribution of Mean Response Times Across All Groups and Categories')
plt.legend(title='Category')
plt.show()


# import pandas as pd
# import matplotlib.pyplot as plt
# import seaborn as sns
# import os
#
# # 设置文件路径
# file_path = '/Users/suecai/Downloads/piggy-microservice/script/data_process/data/'  # 根据实际文件路径调整
#
# # 获取文件列表
# files = sorted([file for file in os.listdir(file_path) if file.endswith('.csv')])
#
# # 读取文件并按组进行处理
# data = {}
# overall_data = {'low': [], 'medium': [], 'high': []}
# for file in files:
#     group = file.split('_grpc')[1].split('.')[0]
#     category = file.split('_')[0]
#     if group not in data:
#         data[group] = {}
#     df = pd.read_csv(os.path.join(file_path, file))
#     data[group][category] = df
#
#     # 将数据添加到 overall_data 中
#     overall_data[category].append(df['Average Response Time'])
#
# # 合并overall_data中的数据
# for category in overall_data:
#     overall_data[category] = pd.concat(overall_data[category], ignore_index=True)
#
# # 进行response time的统计分析
# analysis_results = {}
# for group, categories in data.items():
#     analysis_results[group] = {cat: df['Average Response Time'].mean() for cat, df in categories.items()}
#
# # 设置颜色调色板
# palette = sns.color_palette("Set2", 3)  # 设置为3种颜色
#
# for group, categories in data.items():
#     categories_names = list(categories.keys())
#     means = [df['Average Response Time'].mean() for df in categories.values()]
#
#     # Generate bar plot
#     plt.figure(figsize=(10, 6))
#     sns.barplot(x=categories_names, y=means, hue=categories_names, palette=palette, dodge=False)
#     plt.xlabel('Category')
#     plt.ylabel('Average Response Time')
#     plt.title(f'Group {group} Average Response Time Analysis')
#     plt.show()
#
#     # Generate box plot
#     response_times = pd.concat([df['Average Response Time'] for df in categories.values()], ignore_index=True)
#     labels = []
#     for category in categories_names:
#         labels.extend([category] * len(categories[category]['Average Response Time']))
#
#     plt.figure(figsize=(10, 6))
#     sns.boxplot(x=labels, y=response_times, hue=labels, palette=palette, dodge=False)
#     plt.xlabel('Category')
#     plt.ylabel('Response Time')
#     plt.title(f'Group {group} Response Time Distribution')
#     plt.show()
#
# # Overall boxplot
# overall_response_times = pd.concat([overall_data[cat] for cat in overall_data], ignore_index=True)
# overall_labels = []
# for cat in overall_data:
#     overall_labels.extend([cat] * len(overall_data[cat]))
#
# plt.figure(figsize=(10, 6))
# sns.boxplot(x=overall_labels, y=overall_response_times, palette=palette)
# plt.xlabel('Category')
# plt.ylabel('Response Time')
# plt.title('Overall Response Time Distribution')
# plt.show()
#
# # 生成每组的平均值数据
# mean_values = []
# group_labels = []
# categories_labels = []
#
# for group, categories in data.items():
#     categories_names = list(categories.keys())
#     means = [df['Average Response Time'].mean() for df in categories.values()]
#     for category, mean in zip(categories_names, means):
#         mean_values.append(mean)
#         group_labels.append(f'Group {group}')
#         categories_labels.append(category)
#
# # 将平均值、标签和类别放入数据框中
# mean_df = pd.DataFrame({
#     'Group': group_labels,
#     'Category': categories_labels,
#     'Mean_Response_Time': mean_values
# })
#
# # 绘制箱线图
# plt.figure(figsize=(12, 6))
# sns.boxplot(x='Group', y='Mean_Response_Time', hue='Category', data=mean_df, palette='Set2')
# plt.xlabel('Group')
# plt.ylabel('Mean Response Time')
# plt.title('Distribution of Mean Response Times Across All Groups and Categories')
# plt.legend(title='Category')
# plt.show()