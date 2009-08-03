/**
 * 
 */
package ch.qos.logback.core.rolling.helper;

class SDFToken {
  final char c;
  int occurrences;

  public SDFToken(char c) {
    this.c = c;
    this.occurrences = 1;
  }

  void inc() {
    occurrences++;
  }
}