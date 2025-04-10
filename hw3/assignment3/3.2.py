import math
import os

relevance_dict = {}
with open('average_relevance_filtered.txt', 'r') as file:
    for line in file:
        parts = line.strip().split()
        if len(parts) == 2:
            file_name, score = parts
            relevance_dict[file_name] = int(score)

#print(relevance_dict)


filenames_top50 = []

with open('top50_results.txt', 'r') as file:
    for line in file:
        line = line.strip()
        if line:
            filename = os.path.basename(line)
            filenames_top50.append(filename)

#print(filenames_top50)

filenames_top50_rel_feedback = []

with open('top50_results_rel_feedback.txt', 'r') as file:
    for line in file:
        line = line.strip()
        if line:
            filename = os.path.basename(line)
            filenames_top50_rel_feedback.append(filename)

#print(filenames_top50_rel_feedback)

def compute_dcg(files, rel_dict):
    dcg = 0.0
    for i, fname in enumerate(files):
        if fname == 'Mathematics.f':
            continue
        rel = rel_dict.get(fname, 0)
        if i == 0:
            dcg += rel
        else:
            dcg += rel / math.log2(i + 1)
    return dcg

ideal_files = sorted(relevance_dict.keys(), key=lambda x: relevance_dict[x], reverse=True)[:50]
print(ideal_files)

base_dcg = compute_dcg(filenames_top50, relevance_dict)
dcg = compute_dcg(filenames_top50_rel_feedback, relevance_dict)
idcg = compute_dcg(ideal_files, relevance_dict)

print(base_dcg)
print(dcg)
print(idcg)

base_ndcg = base_dcg / idcg if idcg > 0 else 0.0
print(base_ndcg)


ndcg = dcg / idcg if idcg > 0 else 0.0
print(ndcg)

