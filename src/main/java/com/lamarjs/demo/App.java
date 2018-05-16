package com.lamarjs.demo;


import com.lamarjs.demo.com.lamarjs.someotherpackage.SomeOtherClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private final static Logger log = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {

        log.trace("In the main class. This is a trace level log.");
        log.debug("In the main class. This is a debug level log.");
        log.info("In the main class. This is an info level log.");
        log.warn("In the main class. This is a warn level log.");
        log.error("In the main class This is an error level log.");

        final String lowerCaseString = "it's so small";
        // Usage of {} formatting anchor is more efficient as string concatenation is performed regardless of logging level.
        log.debug("Sending string: {} to be upper-cased", lowerCaseString);

        String upperCasedString = null;
        try {
            upperCasedString = SomeOtherClass.stringToUpper(lowerCaseString);
        } catch (Exception ex) {
            // Adding ex as a final parameter of the log call, the stack trace will be printed automatically by SLF4J.
            // Adding the ex anywhere but last will result in the toString() method being called instead.
            log.error("Caught exception while upper-casing: ", ex);
        }
        log.info("Got back string: {} - printing to console...", upperCasedString);

        System.out.println("Big string is: " + upperCasedString);
    }
}
