import os
import fire
from collections import defaultdict


PRETTY_ONTOLOGY_NAMES = {
    "AboveElbowJacketCast": "Above Elbow Jacket Cast",
    "animals": "Animals",
    "biological-measure-primitive": "Bio-primitive",
    "biosphere": "Biosphere",
    "BNFSection13_11": "BNF Section 13.11",
    "Chlorhexidine": "Chlorhexidine",
    "cl": "Cell",
    "ConeOfTissue": "Cone of Tissue",
    "football": "Football",
    "generations": "Generations",
    "KalliKrein": "Kalli Krein",
    "Neon": "Neon",
    "Pin": "Pin",
    "ProstaglandinDrug": "Prostaglandin Drug",
    "university": "University",
    "Zopiclone": "Zopiclone",
    "Zuccini": "Zuccini",
}

PRETTY_MODEL_NAMES = {
    "llama2:13b": "Llama2 (13b)",
    "llama2": "Llama2 (7b)",
    "llama3": "Llama3 (8b)",
    "llama3.1": "Llama3.1 (8b)",
    "llama3.1:70b": "Llama3.1 (70b)",
    "mistral": "Mistral (7b)",
    "mixtral": "Mixtral (47b)",
    "synthetic": "Synthetic",
    "false": "False"
}

MODEL_TO_CONSIDER = [
    "llama2:13b",
    "llama3",
    "mistral",
    "mixtral",
]

P_VALUE_THRESHOLD = 0.05

def read_metrics_from_file(file_path):
    # print(f"Reading metrics from file: {file_path}")
    with open(file_path, 'r') as file:
        line = file.readline().strip()
        # print(f"Raw metrics line: {line}")
        metrics = list(map(float, line.split()))
        # print(f"Split metrics: {metrics}")
        return metrics


def get_p_value_color(p_value: float, default: str, avg: bool = False) -> str:
    if p_value == 1.0:
        return "\\rowcolor[HTML]{FF0000}"  # red (no axioms learnt)
    elif p_value > P_VALUE_THRESHOLD and not avg:
        return "\\rowcolor[HTML]{FFFF00}"  # opaque yellow
    else:
        return default  # default color

def generate_latex_table(ontology_name, model_metrics):
    print(f"Generating LaTeX table for {ontology_name}")
    table = []
    table.append("\\begin{table*}[]")
    table.append("\\centering")
    table.append("\\resizebox{\\textwidth}{!}{")
    table.append("\\begin{tabular}{|p{2cm}|p{3cm}|p{1.5cm}|p{1.5cm}|p{1.5cm}|p{1.5cm}|}")
    table.append("\\hline")
    table.append("\\multicolumn{1}{|c|}{\\textbf{Model}} & \\multicolumn{1}{|c|}{\\textbf{Prompt \& Query}} & \\multicolumn{1}{c|}{\\textbf{Accuracy}} & \\multicolumn{1}{c|}{\\textbf{Recall}} & \\multicolumn{1}{c|}{\\textbf{Precision}} & \\multicolumn{1}{c|}{\\textbf{F1-Score}} \\\\")
    table.append("\\hline")

    metric_types = ["M. OWL Syntax", "Natural Language", "E. M. OWL Syntax", "E. Natural Language"]

    for j, (model, metrics) in enumerate(model_metrics.items()):
        for i in range(0, len(metrics), 6):
            row = metrics[i:i + 6]
            p_value = row[-1]

            def value_or_none(value: str):
                if p_value == 1.0:
                    return "-"
                else:
                    return value

            row = row[:-2]
            metric_type = metric_types[i // 6]
            # print(f"Metrics for {model} - {metric_type}: {row}")
            if i == 0:
                x = get_p_value_color(p_value, "\\rowcolor[HTML]{EFEFEF}")
                table.append(f"\\multirow{{4}}{{*}}{{{PRETTY_MODEL_NAMES[model]}}} & {x} {metric_type} & " + " & ".join(
                    map(value_or_none, map(str, row))) + " \\\\ \\cline{2-6}")
            elif metric_type == "E. Natural Language":
                x = get_p_value_color(p_value,"\\rowcolor[HTML]{FFFFFF}")
                table.append(f"& {x} {metric_type} & " + " & ".join(map(value_or_none, map(str, row))) + " \\\\ \\cline{1-6}")
            else:
                x = ""
                if i == 6:
                    x = get_p_value_color(p_value, "\\rowcolor[HTML]{FFFFFF}")
                if i == 12:
                    x = get_p_value_color(p_value, "\\rowcolor[HTML]{EFEFEF}")
                table.append(f"& {x} {metric_type} & " + " & ".join(map(value_or_none, map(str, row))) + " \\\\ \\cline{2-6}")
            if i == 18 and j != len(model_metrics) - 1:
                table.append("\\hline")

    table.append("\\end{tabular}}")
    table.append(f"\\caption{{metrics for {ontology_name} ontology.}}")
    table.append("\\end{table*}")
    latex_table = "\n".join(table)
    # print(f"Generated LaTeX table:\n{latex_table}")
    return latex_table


def calculate_averages(metrics_dict, group_by):
    # print(f"Calculating averages grouped by {group_by}...")
    averages = defaultdict(lambda: [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0])

    for key, metrics_list in metrics_dict.items():
        group_key = key[group_by]
        for metrics in metrics_list:
            for i in range(6):  # Accuracy, Recall, Precision, F1-Score, Chi-squared, p-value
                averages[group_key][i] += metrics[i]
            averages[group_key][6] += 1

    for group_key, sums in averages.items():
        for i in range(6):
            sums[i] = round(sums[i] / sums[6], 3)
        # print(f"Averages for {group_key}: {sums[:4]}")

    return averages


def generate_average_latex_table(averages, caption, headers):
    print(f"Generating LaTeX table for {caption}")
    table = []
    table.append("\\begin{table}[]")
    table.append("\\centering")
    table.append("\\resizebox{\\columnwidth}{!}{")
    table.append("\\begin{tabular}{|l|c|c|c|c|}")
    table.append("\\hline")
    headers = [f"\\textbf{{{header}}}" for header in headers]
    table.append(" & ".join(headers) + " \\\\")
    table.append("\\hline")

    for i, (group_key, avg_metrics) in enumerate(averages.items()):
        p_value = avg_metrics[-2]

        def value_or_none(value: str):
            if p_value == 1.0:
                return "-"
            else:
                return value

        if "ontologies" in caption.lower():
            group_key = PRETTY_ONTOLOGY_NAMES[group_key]
        elif "models" in caption.lower():
            group_key = PRETTY_MODEL_NAMES[group_key]
        if i % 2 == 0:
            color = get_p_value_color(p_value, f"\\rowcolor[HTML]{{EFEFEF}}", avg=True)
            table.append(color)
        else:
            color = get_p_value_color(p_value, f"\\rowcolor[HTML]{{FFFFFF}}", avg=True)
            table.append(color)
        table.append(f"{group_key} & " + " & ".join(map(value_or_none, map(str, avg_metrics[:4]))) + " \\\\")
        table.append("\\hline")

    table.append("\\end{tabular}}")
    table.append(f"\\caption{{{caption}}}")
    table.append("\\end{table}")

    latex_table = "\n".join(table)
    # print(f"Generated LaTeX table for {caption}:\n{latex_table}")
    return latex_table


def main(type: str = "simplify"):
    if type=="modules":
        results_dir = "./analysis/modules"
        metric_types = ["E. Natural Language"]
    elif type=="simplify":
        results_dir = "./analysis/simplify"
        metric_types = ["M. OWL Syntax", "Natural Language", "E. M. OWL Syntax", "E. Natural Language"]
    elif type=="synthetic":
        results_dir = "./analysis/synthetic"
        metric_types = ["synthetic"]
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
            for i, metric_type in enumerate(metric_types):
                if type in ["simplify", "modules"]:
                    i = 3
                key = (ontology_name, model, metric_type)
                metrics_dict[key].append(metrics[i * 6:i * 6 + 6])

            ontology_metrics[ontology_name][model].extend(metrics)
            # print(f"Finished processing file: {file_name}\n")

    if type == "simplify":
        for ontology_name, model_metrics in sorted(ontology_metrics.items()):
            latex_table = generate_latex_table(PRETTY_ONTOLOGY_NAMES[ontology_name], model_metrics)
            all_tables.append(latex_table)

    # Calculate and generate average tables
    for group_by, caption, headers in [
        (0, "average metrics grouped by ontologies.", ["Ontology", "Accuracy", "Recall", "Precision", "F1-Score", "Chi-squared", "p-value"]),
        (1, "average metrics grouped by models.", ["Model", "Accuracy", "Recall", "Precision", "F1-Score", "Chi-squared", "p-value"]),
        (2, "average metrics grouped by prompts.", ["Prompts Type", "Accuracy", "Recall", "Precision", "F1-Score", "Chi-squared", "p-value"])
    ]:
        # Remove Llama2 (7b) from the average metrics
        match type:
            case "modules":
                models = MODEL_TO_CONSIDER
            case "synthetic":
                models = ["synthetic"]
            case "simplify":
                models = MODEL_TO_CONSIDER
        metrics_dict = {key: metrics for key, metrics in metrics_dict.items() if key[1] in models}
        averages = calculate_averages(metrics_dict, group_by)
        average_latex_table = generate_average_latex_table(averages, caption, headers[:-2])
        all_tables.append(average_latex_table)
        # print(f"Finished generating LaTeX table for average metrics by {headers[0].lower()}\n")

    # Combine all tables into a single LaTeX document

    file_name = "combined_metrics.tex"
    if type == "modules":
        file_name = "combined_metrics_modules.tex"
    elif type == "synthetic":
        file_name = "combined_metrics_synthetic.tex"
    latex_file_path = os.path.join("./results/latex-tables/", file_name)
    # print(f"Writing combined LaTeX tables to file: {latex_file_path}")
    os.makedirs(os.path.dirname(latex_file_path), exist_ok=True)
    with open(latex_file_path, 'w') as latex_file:
        latex_file.write("\n".join(all_tables))
    # print("Finished writing combined LaTeX file")


if __name__ == "__main__":
    fire.Fire(main)
