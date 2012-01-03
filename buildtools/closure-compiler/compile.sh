#!/bin/sh

java -jar compiler.jar --js ../../src/js/marauroa.js --js ../../src/js/client-framework.js --js ../../src/js/json.js --js ../../src/js/message-factory.js --js ../../src/js/rpfactory.js --js ../../src/js/perception.js --js_output_file ../../src/js/marauroa.compiled.js --compilation_level WHITESPACE_ONLY

