#!/bin/bash

for arg in *.txt
do
  size=$((${#arg}-4))
  title=${arg:0:$size}
  html_name="${arg:0:$size}.php"
  html_name=${html_name// /_}
  html_name="marauroa_$html_name"

  description=`gawk.exe 'BEGIN{FS=":"};{if(match($1,"Info*")){print $2}}' < "$arg"`

echo "\$doc_entry = new Doc;" >> content_php
echo "\$doc_entry->set_data(\"$title\",\"index.php?arianne_url=docs/developer/$html_name\",\"$description\");" >> content_php
echo "\$root->add_child(\$doc_entry);" >> content_php
echo "" >> content_php

  text2html "$arg" > "$html_name"
done