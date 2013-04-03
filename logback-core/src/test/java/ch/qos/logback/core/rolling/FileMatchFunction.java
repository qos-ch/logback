package ch.qos.logback.core.rolling;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 4/3/13
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FileMatchFunction {

  boolean match(File f, String pattern);
}
