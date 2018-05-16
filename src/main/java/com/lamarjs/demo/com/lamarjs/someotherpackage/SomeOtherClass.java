package com.lamarjs.demo.com.lamarjs.someotherpackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SomeOtherClass {

    private final static Logger log = LoggerFactory.getLogger(SomeOtherClass.class);

    static public String stringToUpper(String string) throws Exception {
        log.debug("In SomeOtherClass.stringToUpper. Received string: {}", string);
        String upperCased = string.toUpperCase();
        log.debug("Returning upper-cased string: {}", upperCased);

        // Let's assume we have requirements for our upperCaseFunction to refuse to do it's work half the time:
        Random random = new Random();

        if (random.nextBoolean()) {
            log.warn("You want me to uppercase something for you... Meh.");
            throw new Exception("I don't feel like upper-casing your string.");
        }

        return upperCased;
    }
}
