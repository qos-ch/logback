
package joran.helloWorld;



import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ExecutionContext;


/**
 * A trivial action that writes "Hello world" on the console.
 * 
 * See the HelloWorld class for integrating with Joran.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class HelloWorldAction extends Action {
  public void begin(ExecutionContext ec, String name, Attributes attributes) {
    System.out.println("Hello World");
  }

  public void end(ExecutionContext ec, String name) {
  }
}
