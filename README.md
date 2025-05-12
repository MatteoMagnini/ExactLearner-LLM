 
# ExactLearner+LLM
 
This is a Java implementation of the **ExactLearner+LLM** tool proposed in the paper
_"Actively Learning EL Terminologies from Large Language Models"_, submitted to **ECAI 2025**.
In this same repository you can find the experiments and results presented in the paper.

## General information

This repository contains both the code of the ExactLearner+LLM tool and the code for the experiments.
The structure of the repository is as follows:
- `src/main/java/exactlearner` contains the code of the ExactLearner+LLM tool.
- `src/main/java/experiments` contains the code for the experiments.
- `src/main/resources/ontologies` contains the ontologies used in the experiments.
- `results/ontologies` contains the learnt ontologies.
- `analysis` contains the metrics computed over the learnt ontologies.

## How to use

> Requirements:
> - Java 21
> - Maven
> - sqlite3

This is a Maven project. To run the ExactLearner+LLM tool, you need to have Maven installed.
To install Maven, follow the instructions in the official website: https://maven.apache.org/install.html

To install the dependencies, run the following command in the root directory of the project:
```bash
mvn install
```
To compile the project, run the following command:
```bash
mvn compile
```

The project uses a cache system based on **sqlite3** to store the results of the queries.
To install sqlite3, follow the instructions in the official website: https://www.sqlite.org/download.html

## About the experiments

Run the experiments using the following commands:
1. Run the ExactLearner+LLM tool:
    ```bash
     mvn exec:java -Dexec.mainClass="org.experiments.LaunchLLMLearner" -Dexec.args="<config_file>"
    ```
    Where `<config_file>` is the path to the configuration file among the following:
   1. `src/main/java/org/configurations/experiments/manchester-simple.yml` for simple prompt in Manchester OWL syntax on the small ontologies using all LLMs;
   2. `src/main/java/org/configurations/experiments/manchester-advanced.yml` for advanced prompt in Manchester OWL syntax on the small ontologies using all LLMs;
   3. `src/main/java/org/configurations/experiments/nlp-simple.yml` for simple prompt in natural language on the small ontologies using all LLMs;
   4. `src/main/java/org/configurations/experiments/nlp-advanced.yml` for advanced prompt in natural language on the small ontologies using all LLMs;
   5. `src/main/java/org/configurations/experiments/moduls.yml` for advanced prompt in natural language on the modules ontologies Mistral.
2. Run the ExactLearner+LLM tool with the synthetic teacher:
   ```bash
    mvn exec:java -Dexec.mainClass="org.experiments.LaunchExactLearner" -Dexec.args="<config_file>"
    ```
    Where `<config_file>` is the path to the configuration file among the following:
   1. `src/main/java/org/configurations/experiments/nlp-advanced.yml` to run the tool on the small ontologies;
   2. `src/main/java/org/configurations/experiments/moduls.yml` to run the tool on the modules ontologies Mistral.

> **IMPORTANT 1:** inside the connection package (`src/main/java/org/exactlearnerconnection`) you may change the URLs of the LLMs services.

> **IMPORTANT 2:** running the experiments may take a long time, depending on the number of queries and the LLMs used.