
package joran.helloWorld;



import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;


/**
 *
 * A hello world example using Joran.
 *
 * The first and only argument of this application must be the path to
 * the XML file to interpret.
 *
 * For example,
 *
<pre>
    java joran.helloWorld.HelloWorld examples/src/joran/helloWorld/hello.xml
</pre>
 *
 * @author Ceki
 */
public class HelloWorld {
  public static void main(String[] args) throws Exception {
    // Create a simple rule store where pattern and action associations will
    // be kept.
    RuleStore ruleStore = new SimpleRuleStore(null);

    // Associate "hello-world" pattern with  HelloWorldAction
    ruleStore.addRule(new Pattern("hello-world"), new HelloWorldAction());

    // Create a new Joran Interpreter and hand it our simple rule store.
    Interpreter ji = new Interpreter(ruleStore);

    // Create a SAX parser
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();

    // Parse the file given as the application's first argument and
    // set the SAX ContentHandler to the Joran Interpreter we just created.
    saxParser.parse(args[0], ji);
  }
}
