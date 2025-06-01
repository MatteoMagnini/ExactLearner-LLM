#!/bin/bash
if [ $# -lt 2 ]
then
  exit 1;
fi

cd /home/dev/persistent/ExactLearner || { exit 127; }
mvn clean install -DskipTests
mvn exec:java -Dexec.mainClass="org.analysis.specialized.ResultAnalyzer" -Dexec.args="src/main/java/org/configurations/exp3/medical/Llama3ENLP.yml $1 $2"
mvn exec:java -Dexec.mainClass="org.analysis.specialized.ResultAnalyzer" -Dexec.args="src/main/java/org/configurations/exp3/medical/MixtralMistralNLP.yml $1 $2"
