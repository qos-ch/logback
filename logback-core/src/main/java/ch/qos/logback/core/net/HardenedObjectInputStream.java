package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2.0
 */
public class HardenedObjectInputStream extends ObjectInputStream {

    List<String> whitelistedClassNames;
    String[] javaPackages = new String[] {"java.lang", "java.util"};
    
    public HardenedObjectInputStream(InputStream in, List<String> whilelist) throws IOException {
        super(in);
        this.whitelistedClassNames = Collections.synchronizedList(new ArrayList<String>(whilelist));
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass anObjectStreamClass) throws IOException, ClassNotFoundException {
        String incomingClassName = anObjectStreamClass.getName();
        if(!isWhitelisted(incomingClassName)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", anObjectStreamClass.getName());
        }
    
        return super.resolveClass(anObjectStreamClass);
    }

    private boolean isWhitelisted(String incomingClassName) {
        for(int i = 0; i < javaPackages.length; i++) {
            if(incomingClassName.startsWith(javaPackages[i]))
                return true;
        }
        for(String className: whitelistedClassNames) {
            if(incomingClassName.equals(className))
                return true;
        }
        return false;
    }
}
