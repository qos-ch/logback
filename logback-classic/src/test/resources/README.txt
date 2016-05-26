
Few test cases InitializationTest require the presence of logback.xml or logback-test.xml
to be present in the classpath. However, this conflict with the logback-examples module. 
In particular, when users attempt to follow the manual by importing the project in an IDE 
such as Eclipse.