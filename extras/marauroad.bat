set LOCALCLASSPATH=.;marauroa-3.9.8.jar;libs/jython.jar;libs/h2.jar;libs/mysql-connector.jar;libs/log4j.jar;libs/json-simple-1.1.1.jar;libs/jakarta.annotation-api-2.1.1.jar;libs/tomcat-embed-core-10.1.7.jar;libs/tomcat-embed-websocket-10.1.7.jar
java -cp "%LOCALCLASSPATH%" marauroa.server.marauroad -c server.ini -l
