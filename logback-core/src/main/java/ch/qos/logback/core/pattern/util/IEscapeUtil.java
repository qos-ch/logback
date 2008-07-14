package ch.qos.logback.core.pattern.util;

public interface IEscapeUtil {

  void escape(String additionalEscapeChars, StringBuffer buf, char next, int pointer);
}