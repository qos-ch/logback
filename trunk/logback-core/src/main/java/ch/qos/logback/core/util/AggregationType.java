package ch.qos.logback.core.util;

/**
 * AggregationType classifies how one object is contained within 
 * another object.
 * 
 * 
 * 
 * See also http://en.wikipedia.org/wiki/Class_diagram
 * and http://en.wikipedia.org/wiki/Object_composition
 * 
 * @author Ceki Gulcu
 */
public enum AggregationType {
  NOT_FOUND, 
  AS_BASIC_PROPERTY, // Long, Integer, Double,..., java primitive, String,
                      // Duration or FileSize
  AS_COMPLEX_PROPERTY, // a complex property, a.k.a. attribute, is any attribute 
                       // not covered by basic attributes, i.e. 
                       // object types defined by the user
  AS_BASIC_PROPERTY_COLLECTION, // a collection of basic attributes
  AS_COMPLEX_PROPERTY_COLLECTION; // a collection of complex attributes
}
