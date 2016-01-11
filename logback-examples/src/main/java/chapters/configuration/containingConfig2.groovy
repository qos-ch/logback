Script child = include("src/main/java/chapters/configuration/includedConfig2.groovy")
println "I am " + this + " and my child is " + child
println "Child method call output: " + child.provideIncludedValue()
println "Child field value: " + child.includedField

root(DEBUG, ["includedConsole"])

@Field
String containingField = "Value of containing field"

String provideContainingValue() {
  return "Value returned by method in containing script"
}