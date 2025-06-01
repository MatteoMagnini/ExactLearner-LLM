#!/bin/bash

for file in *ic_metrics.csv; do
	startthing=$(echo "$file" | sed 's/_/,/g' | sed 's/GO,00/GO_00/' | sed 's/metrics.csv//')
	ce=$(tail -n 1 $(echo "$file" | sed 's/metrics\.csv/sizes.csv/') | awk -F',' '{print $1 "," $5 "," $6}')
	echo "" | awk -v st="$startthing" -v ce="$ce" '{print st $0 "," ce}'
done
