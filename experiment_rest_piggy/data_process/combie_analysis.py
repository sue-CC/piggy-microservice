import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from statsmodels.formula.api import ols
from statsmodels.stats.anova import anova_lm

data = pd.read_csv("combined_data_rest.csv")

data.columns = ['response_time', 'type', 'frequency', 'size']

print(data.info())
print(data.describe())

print(data.isnull().sum())

desc_stats = data.groupby(['size', 'frequency'])['response_time'].describe()
print(desc_stats)

mean_response_time = data.pivot_table(values='response_time', index='size', columns='frequency', aggfunc='mean')
print("Mean Response Time for each (Size, Frequency) combination:")
print(mean_response_time)

plt.figure(figsize=(12, 6))

plt.subplot(2, 2, 1)
sns.barplot(x='size', y='response_time', data=data, palette='viridis')
plt.title('Bar Plot: Response Time by Size')

plt.subplot(2, 2, 2)
sns.barplot(x='frequency', y='response_time', data=data, palette='viridis')
plt.title('Bar Plot: Response Time by Frequency')

plt.subplot(2, 2, 3)
sns.boxplot(x='size', y='response_time', data=data)
plt.title('Box Plot: Response Time by Size')

plt.subplot(2, 2, 4)
sns.boxplot(x='frequency', y='response_time', data=data)
plt.title('Box Plot: Response Time by Frequency')

plt.tight_layout()
plt.show()

sns.lmplot(x='size', y='response_time', hue='frequency', data=data, aspect=1.5)
plt.title('Response Time by Size and Frequency')
plt.show()

formula = 'response_time ~ C(size) + C(frequency) + C(size):C(frequency)'
model = ols(formula, data=data).fit()
anova_results = anova_lm(model)
print(anova_results)

print(model.summary())

size_mapping = {0: 'Small', 1: 'Medium', 2: 'Large'}
frequency_mapping = {0: 'Low', 1: 'Medium', 2: 'High'}
data['Size'] = data['size'].map(size_mapping)
data['Frequency'] = data['frequency'].map(frequency_mapping)

frequency_order = ['Low', 'Medium', 'High']
size_order = ['Small', 'Medium', 'Large']

plt.figure(figsize=(10, 6))
sns.barplot(x='Frequency', y='response_time', hue='Frequency', data=data, palette='viridis', order=frequency_order, dodge=False)
plt.title('Bar Plot: Frequency vs. Average Response Time')
plt.xlabel('Frequency')
plt.ylabel('Average Response Time')
plt.legend([], [], frameon=False)
plt.show()

plt.figure(figsize=(10, 6))
sns.barplot(x='Size', y='response_time', hue='Size', data=data, palette='viridis', order=size_order, dodge=False)
plt.title('Bar Plot: Size vs. Average Response Time')
plt.xlabel('Size')
plt.ylabel('Average Response Time')
plt.legend([], [], frameon=False)
plt.show()

plt.figure(figsize=(12, 8))
sns.barplot(x='size', y='response_time', hue='frequency', data=data, palette='viridis')
plt.title('Bar Plot: Response Time by Size and Frequency Combination')
plt.xlabel('Size')
plt.ylabel('Response Time')
plt.legend(title='Frequency')
plt.show()

plt.figure(figsize=(12, 8))
sns.boxplot(x='size', y='response_time', hue='frequency', data=data, palette='viridis')
plt.title('Box Plot: Response Time by Size and Frequency Combination')
plt.xlabel('Size')
plt.ylabel('Response Time')
plt.legend(title='Frequency')
plt.show()