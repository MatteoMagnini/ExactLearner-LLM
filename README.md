 
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
> - Java 17
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
TODO: how to run the experiments.