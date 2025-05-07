#!/bin/bash
cd $1 || { exit 127; }

mkdir results/ontologies/temp_store
mkdir analysis/temp_store

for file in results/ontologies/*; do
  [ -f "$file" ] && mv "$file" results/ontologies/temp_store
done

for file in analysis/*; do
  [ -f "$file" ] && mv "$file" analysis/temp_store
done

function run_test {
  export EXACTLEARNER_OLLAMA_URL="http://localhost:11434/api/generate"
  export EXACTLEARNER_SPLIT=$1
  export EXACTLEARNER_DESATURATE=$2

  mvn clean install -DskipTests
  mvn exec:java -Dexec.mainClass="org.experiments.exp2.LaunchLLMLearner" -Dexec.args="src/main/java/org/configurations/statementsQueryingConf.yml"
  mvn exec:java -Dexec.mainClass="org.experiments.exp2.LaunchLLMLearner" -Dexec.args="src/main/java/org/configurations/statementsQueryingConf2.yml"
  mvn exec:java -Dexec.mainClass="org.experiments.exp2.LaunchLLMLearner" -Dexec.args="src/main/java/org/configurations/statementsQueryingConfAdvanced.yml"
  mvn exec:java -Dexec.mainClass="org.experiments.exp2.LaunchLLMLearner" -Dexec.args="src/main/java/org/configurations/statementsQueryingConfTrueFalse.yml"

  mvn exec:java -Dexec.mainClass="org.analysis.exp2.ResultAnalyzer" -Dexec.args="src/main/java/org/configurations/statementsQueryingConf.yml"

  mkdir -p results/ontologies/$3

  for file in results/ontologies/*; do
    [ -f "$file" ] && mv "$file" results/ontologies/$3
  done

  mkdir -p analysis/$3

  for file in analysis/*; do
    [ -f "$file" ] && mv "$file" analysis/$3
  done
}

run_test "false" "false" "none"

run_test "true" "false" "split"

run_test "false" "true" "desaturate"

run_test "true" "true" "simplify"

mv results/ontologies/temp_store/* results/ontologies
rmdir results/ontologies/temp_store

mv analysis/temp_store/* analysis/
rmdir analysis/temp_store
