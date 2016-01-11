println "I am " + this + " and my parent is " + parent
println "Parent method call output: " + parent.provideContainingValue()
println "Parent field value: " + parent.containingField

@Field
String includedField = "Value of included field"

String provideIncludedValue() {
  return "Value returned by method in included script"
}

appender("includedConsole", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = '"%d - %m%n"'
  }
}