import numpy as np
import matplotlib.pyplot as plt




def plot_precision_recall_vs_cutoff(cutoffs, precision, recall):

    plt.figure(figsize=(12, 5))

    plt.subplot(1, 2, 1)
    plt.plot(cutoffs, precision, marker='o', linestyle='-', color='blue', label='Precision')
    plt.xticks(cutoffs)
    plt.xlabel('Cutoff')
    plt.ylabel('Precision')
    plt.title('Precision')
    plt.legend()

    plt.subplot(1, 2, 2)
    plt.plot(cutoffs, recall, marker='o', linestyle='-', color='red', label='Recall')
    plt.xlabel('Cutoff')
    plt.ylabel('Recall')
    plt.xticks(cutoffs)
    plt.title('Recall')
    plt.legend()

    plt.tight_layout()
    plt.savefig('Precision_recall.png')
    plt.show()




if __name__ == '__main__':
    relevance_document = np.array([1, 1, 3, 4, 4])
    cutoffs = np.array([10, 20, 30, 40, 50])
    precision_values = relevance_document / cutoffs
    print(precision_values)
    recall_values = relevance_document / 100

    plot_precision_recall_vs_cutoff(cutoffs, precision_values, recall_values)
