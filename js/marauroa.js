var marauroa = new function() {}

marauroa.log = {};
marauroa.log.debug = console.debug || function(text) {};
marauroa.log.info = console.info || function(text) {};
marauroa.log.warn = console.warn || function(text) {};
marauroa.log.error = console.error || function(text) {alert(text)};
