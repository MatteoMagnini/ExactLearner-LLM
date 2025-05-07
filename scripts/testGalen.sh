#!/bin/bash
cd $1 || { exit 127; }

export EXACTLEARNER_OLLAMA_URL="http://localhost:11434/api/generate"

mkdir -p tmp

rm tmp/modules.txt

mvn clean install -DskipTests

for file in $(find "$2" -type f | sort | shuf --random-source=<(yes 42) -n 10); do
    echo "Running test on $file"

    echo "$file" >> tmp/modules.txt

    # Yes, i know this looks bad
    echo "models:
  - \"mistral\"
ontologies:
  - \"$file\"
system: >
  You need to classify the following statements as True or False. The statement will be provided in either Manchester OWL syntax or natural language. Strictly follow these guidelines:
  1. answer with only True or False;
  2. entities with has part relation are not in a subclass relation;
  3. take a deep breath before answering;
  4. if you are unsure about the classification, answer with False.
maxTokens: 2
queryFormat: \"nlp\"
type: \"statementsQuerying\"
" > tmp/test_file.yml
    
    timeout 2h mvn exec:java -Dexec.mainClass="org.experiments.exp2.LaunchLLMLearner" -Dexec.args="tmp/test_file.yml"
done
