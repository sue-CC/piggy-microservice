import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from statsmodels.formula.api import ols
from statsmodels.stats.anova import anova_lm

# 读取数据
data = pd.read_csv("combined_data.csv")

# 重命名列名以便代码处理
data.columns = ['response_time', 'type', 'frequency', 'size']

# 查看数据基本信息
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

# 箱线图：请求大小对响应时间的影响
plt.subplot(1, 2, 1)
sns.boxplot(x='size', y='response_time', data=data)
plt.title('Response Time by Size')

# 箱线图：频率对响应时间的影响
plt.subplot(1, 2, 2)
sns.boxplot(x='frequency', y='response_time', data=data)
plt.title('Response Time by Frequency')

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

