#!/bin/bash

for arg in *.txt
do
  size=$((${#arg}-4))
  html_name="${arg:0:$size}.html"
  html_name=${html_name// /_}
  text2html "$arg" > "$html_name"
done