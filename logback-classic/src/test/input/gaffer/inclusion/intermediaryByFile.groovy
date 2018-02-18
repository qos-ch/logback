p = "HELLO"
String subFileKey = System.properties.getProperty("subFileKey")
include(subFileKey)
root(DEBUG, ["STDOUT"])