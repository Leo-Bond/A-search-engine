import numpy as np
import matplotlib.pyplot as plt

numbers1 = np.loadtxt("mc1_squared_differences.txt")
numbers2 = np.loadtxt("mc2_squared_differences.txt")
numbers3 = np.loadtxt("mc4_squared_differences.txt")
numbers4 = np.loadtxt("mc5_squared_differences.txt")
plt.figure(figsize=(8, 8))
plt.plot(numbers1, linestyle='-', label='MC1 squared difference')
plt.plot(numbers2, linestyle='-', label='MC2 squared difference')
plt.plot(numbers3, linestyle='-', label='MC4 squared difference')
plt.plot(numbers4, linestyle='-', label='MC5 squared difference')
plt.xlabel("nomOfdoc * 5x")
plt.ylabel("sum_squared_difference")
plt.legend(loc='best')
plt.grid(True)
plt.savefig("mc.jpg")
plt.show()
