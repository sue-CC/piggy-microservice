import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from statsmodels.formula.api import ols
from statsmodels.stats.anova import anova_lm

# 读取数据
data = pd.read_csv("combined_data.csv")

# 重命名列名以便代码处理
data.columns = ['response_time', 'type', 'frequency', 'size']

# 检查数据基本信息
print(data.info())
print(data.describe())

# 检查是否有缺失值
print(data.isnull().sum())

# 描述性统计分析
desc_stats = data.groupby(['size', 'frequency'])['response_time'].describe()
print(desc_stats)

# 计算不同size和frequency组合下的平均response time
mean_response_time = data.pivot_table(values='response_time', index='size', columns='frequency', aggfunc='mean')
print("Mean Response Time for each (Size, Frequency) combination:")
print(mean_response_time)

# 数据可视化
plt.figure(figsize=(12, 6))

# 柱状图：请求大小对响应时间的影响
plt.subplot(2, 2, 1)
sns.barplot(x='size', y='response_time', data=data, palette='viridis')
plt.title('Bar Plot: Response Time by Size')

# 柱状图：频率对响应时间的影响
plt.subplot(2, 2, 2)
sns.barplot(x='frequency', y='response_time', data=data, palette='viridis')
plt.title('Bar Plot: Response Time by Frequency')

# 箱线图：请求大小对响应时间的影响
plt.subplot(2, 2, 3)
sns.boxplot(x='size', y='response_time', data=data)
plt.title('Box Plot: Response Time by Size')

# 箱线图：频率对响应时间的影响
plt.subplot(2, 2, 4)
sns.boxplot(x='frequency', y='response_time', data=data)
plt.title('Box Plot: Response Time by Frequency')

plt.tight_layout()
plt.show()

# 散点图和线性回归拟合
sns.lmplot(x='size', y='response_time', hue='frequency', data=data, aspect=1.5)
plt.title('Response Time by Size and Frequency')
plt.show()

# 方差分析 (ANOVA)
formula = 'response_time ~ C(size) + C(frequency) + C(size):C(frequency)'
model = ols(formula, data=data).fit()
anova_results = anova_lm(model)
print(anova_results)

# 回归模型
print(model.summary())

# 将数值映射为文本
size_mapping = {0: 'Small', 1: 'Medium', 2: 'Large'}
frequency_mapping = {0: 'Low', 1: 'Medium', 2: 'High'}
data['Size'] = data['size'].map(size_mapping)
data['Frequency'] = data['frequency'].map(frequency_mapping)

# 保持频率和大小的顺序进行绘图
frequency_order = ['Low', 'Medium', 'High']
size_order = ['Small', 'Medium', 'Large']

# 平均响应时间的柱状图（按频率排序）
plt.figure(figsize=(10, 6))
sns.barplot(x='Frequency', y='response_time', hue='Frequency', data=data, palette='viridis', order=frequency_order, dodge=False)
plt.title('Bar Plot: Frequency vs. Average Response Time')
plt.xlabel('Frequency')
plt.ylabel('Average Response Time')
plt.legend([], [], frameon=False)
plt.show()

# 平均响应时间的柱状图（按大小排序）
plt.figure(figsize=(10, 6))
sns.barplot(x='Size', y='response_time', hue='Size', data=data, palette='viridis', order=size_order, dodge=False)
plt.title('Bar Plot: Size vs. Average Response Time')
plt.xlabel('Size')
plt.ylabel('Average Response Time')
plt.legend([], [], frameon=False)
plt.show()

# 绘制九种频率和大小组合的柱状图
plt.figure(figsize=(12, 8))
sns.barplot(x='size', y='response_time', hue='frequency', data=data, palette='viridis')
plt.title('Bar Plot: Response Time by Size and Frequency Combination')
plt.xlabel('Size')
plt.ylabel('Response Time')
plt.legend(title='Frequency')
plt.show()

# 绘制九种频率和大小组合的箱线图
plt.figure(figsize=(12, 8))
sns.boxplot(x='size', y='response_time', hue='frequency', data=data, palette='viridis')
plt.title('Box Plot: Response Time by Size and Frequency Combination')
plt.xlabel('Size')
plt.ylabel('Response Time')
plt.legend(title='Frequency')
plt.show()
