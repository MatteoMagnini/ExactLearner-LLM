import os
from collections import defaultdict

PRETTY_ONTOLOGY_NAMES = {
    "animals": "Animals",
    "university": "University",
    "cl": "Cell",
    "generations": "Generations",
    "biosphere": "Biosphere",
    "football": "Football",
    "biological-measure-primitive": "Bio-primitive",
}

PRETTY_MODEL_NAMES = {
    "llama2:13b": "Llama2 (13b)",
    "llama2": "Llama2 (7b)",
    "llama3": "Llama3 (8b)",
    "llama3.1": "Llama3.1 (8b)",
    "llama3.1:70b": "Llama3.1 (70b)",
    "mistral": "Mistral (7b)",
    "mixtral": "Mixtral (47b)",
    "false": "False"
}

MODEL_TO_CONSIDER = [
    "llama2:13b",
    "llama3",
    "mistral",
    "mixtral",
]



def read_metrics_from_file(file_path):
    # print(f"Reading metrics from file: {file_path}")
    with open(file_path, 'r') as file:
        line = file.readline().strip()
        # print(f"Raw metrics line: {line}")
        metrics = list(map(float, line.split()))
        # print(f"Split metrics: {metrics}")
        return metrics

def main():
    for i in ["none", "split", "desaturate", "simplify"]:
        print(i)
        results_dir = f"./analysis/{i}/"
        # print(f"Reading files from directory: {results_dir}")
        metrics_dict = defaultdict(list)
        all_tables = []

        ontology_metrics = defaultdict(lambda: defaultdict(list))

        for file_name in sorted(os.listdir(results_dir)):
            if file_name.endswith(".txt"):
                # print(f"Processing file: {file_name}")
                file_path = os.path.join(results_dir, file_name)
                file_name = file_name.replace('-13b', '_13b')
                file_name = file_name.replace('-70b', '_70b')
                parts = file_name.replace('.txt', '').split('-')
                ontology_name = '-'.join(parts[:-1])
                model = parts[-1]
                model = model.replace('_', ':')
                # print(f"Ontology: {ontology_name}, Model: {model}")
                metrics = read_metrics_from_file(file_path)
                metrics = [round(metric, 3) for metric in metrics]

                metric_types = ["M. OWL Syntax", "Natural Language", "E. M. OWL Syntax", "E. Natural Language"]
                for i, metric_type in enumerate(metric_types):
                    key = (ontology_name, model, metric_type)
                    metrics_dict[key].append(metrics[i * 6:i * 6 + 6])

                ontology_metrics[ontology_name][model].extend(metrics)
                # print(f"Finished processing file: {file_name}\n")

        models = {}
        n = 0
        
        for ontology_name, model_metrics in sorted(ontology_metrics.items()):
            if ontology_name not in ["animals", "football", "generations", "university", "cl"]:
                continue
            n+=1
            for model_name, model_metric in model_metrics.items():
                if model_name not in ["llama2:13b", "llama3", "mistral", "mixtral"]:
                    continue
                if model_name not in models:
                    models[model_name] = model_metric
                else:
                    l = models[model_name]
                    for i in range(len(l)):
                        l[i] += model_metric[i]

        print(f"model,setting,Accuracy,Recall,Precision,F1-score")
        for k, v in models.items():
            for i, s in enumerate(["M.Syntax", "NLP", "E. M.Syntax", "E. NLP"]):
                scors = ",".join([str(a/n) for a in v[i*6:i*6+6]])
                print(f"{k},{s},{scors}")

if __name__ == "__main__":
    main()
