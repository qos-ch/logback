#--------------------------#
|                          |
|   Log4j-bridge Read-me   |
|                          |
#--------------------------#

This directory is used to test the module against various log4j calls. 
Two test cases simulate the typical calls that one can find in an application 
that uses either log4j 1.2.x, or log4j 1.3.x.

In the same directory is a build.xml file that uses ant to 
compile the test cases with the corresponding log4j version, 
and to runs these tests without log4j in the classpath but with 
logback jars instead.

To run the tests, one must have ant installed. Issuing the following command, 
once in the compatibility directory will launch the tests:

ant all

To obtain more information about the use of the log4j-bridge module, 
please visit http://logback.qos.ch/bridge.html