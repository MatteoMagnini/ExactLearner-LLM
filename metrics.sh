#!/bin/bash

for file in *metrics.csv; do
	startthing=$(echo "$file" | sed 's/_/,/g' | sed 's/GO,00/GO_00/' | sed 's/metrics.csv//')
	ce=$(tail -n 1 $(echo "$file" | sed 's/metrics\.csv/sizes.csv/') | awk -F',' '{print $(NF-1) "," $NF}')
	tail -n 1 $file | awk -v st="$startthing" -v ce="$ce" '{print st $0 "," ce}'
done
