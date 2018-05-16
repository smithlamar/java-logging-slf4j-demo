~~~~~~~~~~~~~~~~
~ Java Logging ~
~~~~~~~~~~~~~~~~

---------------------------------------
- Logging Facades vs. Implementations -
---------------------------------------

    + Logging Implementations: These are direct implementations of logging tools, i.e. you can use them (call their API's) directly. When you want to change your logging provider, you will need to update your logging calls to make sense to whatever you switch to.
        Pros
        ----
        - You will generally require fewer dependencies for logging in your app.

        Cons
        ----
        - It can be more difficult for you to switch to other implementations because you may need to update the actual log statements in your code.

        Options
        -------
        - JUL(java.util.logging) : built in java logging api. Differs quite a bit from most others. Probably the least popular and for good reason.

        - Apache Log4j 1.2 | Log4j2 : good options. Use the later for greenfield. Major improvements in 2 vs 1.2 especially in regard to performance/latency. Log4j2 is supported by Spring (after some minor configuration is performed).

        - Logback : another good option, configuration can be xml or groovy which is good flexibility to have. Has some degree of first-class support in Spring. ** Logback is a unique implementation as it is native to the SLF4J facade. i.e. SLF4J doesn't translate Logback calls, it uses it directly.

    + Logging Facades (Frameworks): These are glorified logging APIs that act as translators or fronts for the underlying logging implementations that can power them. When picking a framework, you also need to select a logging implementation that it supports . You don't have to call the implementation directly so you can easily switch logging implementations without touching your logging statements. Facades will also bridge (convert) logs from other logging implementations and frameworks that it supports.

        Pros
        ----
        - The frameworks provide a "standardized" set of logging APIs that makes it easy to flip from one underlying logging implementation to the other i.e. for gains in performance/speed or ease of config management.

        - You can also bridge other logging frameworks used by your code's dependencies to your logging framework so that you can reason with them from one general point of configuration.

        Cons
        ----
        - Tends to require a lot of dependencies imported by your build tool (i.e. gradle). These dependencies are usually not very clear.
        - Configuration of settings for logging can still be somewhat dependent on the underlying implementation.

        Options
        --------
        - Apache Commons Logging: Facade for JUL or Log4j1.2

        - SLF4J: Simple relatively straightforward API. Synergizes well with Logback. Facade for Log4J2 as well.

        - Log4J2: yes it is an implementation, but it can also be a facade over SLF4J - which itself is a facade that can front Log4j2 (yes this is meant to confuse you).


--------------------
- Bridge VS Facade -
--------------------

    + When you pick a facade like SLF4J, you also need to pick an implementation like logback.

    +You can then bridge any other implementations/frameworks that SLF4J supports (likely being used by your code's dependencies) to logback. This allows you to control your dependencies log configuration to some degree as well.

    Options
    ------
    SLF4J can bridge: Log4j1.2, and Apache Commons Logging, and JUL.
    Log4J2 can bridge: Log4j1.2 and Apache Commons Logging.


---------------------------
- My Recommended approach -
---------------------------

Use SLF4J as your facade and Log4j2 as your implementation. You can then bridge pretty much anything else you would encounter in your dependencies to SLF4J.

--------------------------------
- Setting up SLF4J over Log4j2 -
--------------------------------

Details of Log4j2 as an implementation over SLF4J facade can be found here:
https://logging.apache.org/log4j/2.0/runtime-dependencies.html
and for a detailed dependency tree complete with licenses for each component and transitive dependency: https://logging.apache.org/log4j/2.0/log4j-slf4j-impl/dependencies.html#Dependency_Tree

- The Short Version -

1. import the following:

Gradle:

dependencies {
    // SLF4J as a facade over Log4j2 required dependencies
    compile group: 'org.apache.logging.log4j', name: 'log4j-api', version: '2.11.0'
    compile group: 'org.apache.logging.log4j', name: 'log4j-core', version: '2.11.0'
    compile group: 'org.apache.logging.log4j', name: 'log4j-slf4j-impl', version: '2.11.0'

    // Bridges from other logging implementations to SLF4J. Be careful not to bridge SLF4J to log4j2 in the other direction by mistake.
    compile group: 'org.slf4j', name: 'jul-to-slf4j', version: '1.7.25' // JUL bridge
    compile group: 'org.slf4j', name: 'jcl-over-slf4j', version: '1.7.25' // Apache Commons Logging (JCL) bridge
    compile group: 'org.slf4j', name: 'log4j-over-slf4j', version: '1.7.25' // log4j1.2 bridge

Maven:
<!-- Generally get the latest release version available for these dependencies if starting a new project unless you have a very good reason to use an older version. That version will usually be applied across the entire set of dependencies. -->
<dependencies>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.11.0</version>
    </dependency>

    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.11.0</version>
    </dependency>

    <!-- This one adds slf4j core libraries and turns log4j2 into the implementation. -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.11.0</version>
    </dependency>

     etc...

</dependencies>


2. Instantiate your logger for your class. Doing so statically is the common idiom and comes with (very slightly) less performance overhead:

    // Be sure to import from org.slf4j for Logger and LoggerFactory
    private final static Logger log = LoggerFactory.getLogger(MyClass.class);


3. Log stuff
    log.debug("This is an 'debug' level log statement. Depending on configuration settings in my app, this may not show when I run my program.");

    // Use parameterized vars for performance gains instead of concatenation.
    String someVariable = "foo";
    log.debug("My string is: {}", someVariable);


--------------------
- Logging Strategy -
--------------------

    1. At a bare minimum, catch exceptions and print the stack trace. You may not be able to do more than that about the exception but the stack trace can save you when you are trying to figure out what went wrong.
    2. Your logs should read like a narrative of what is happening in your code.
        - "Processing started of foos"
        - "Got foo with id: 12345 - processing started:"
        - "Foo 12345 baz property already set to 'palindromes'"
        - "No enrichment of baz property needed for foo 1"
        - "Got foo with id: 678910"
        - "Foo 678910 has no value for baz property"
        - "Calling bar to enrich baz property on foo 2"
        - "Foo 678910 baz property set to 'fizzbuzz'."
        - "All foos processed"

    3. It can be easy to have misleading statements like "failed because foo service not available". But if you're catching a general excetpion, even if you think you know what the cause will always be, you actually don't, so you can be stating the wrong cause in your log when you catch the exception. Only have specific failure reasons when you are catching an explicit exception type.

    4. It's better to log more than what you need rather than less. Otherwise, you will be a sad puppy when you're app is deployed to production.

-----------------
- configuration -
-----------------

    - Log4j2 configuration can be managed via xmla, json, yaml, or .properties files. It can also be managed programaticlly through ConfigurationFactory and the Configuration class as well as by calling methods on the internal Logger class.

    - Log4j2 will attempt to find/resolve configuration in the following order:

        1. The path specified in the system property: "log4j.configurationFile"
        2. log4j2-test.properties
        3. log4j2-test.yaml|yml
        4. log4j2-test.json|jsn
        5. log4j2-test.xml
        6. log4j2.properties
        7. log4j2.yaml|yml
        8. log4j2.json|jsn
        9. log4j2.xml
        10. DefaultConfiguration will be used which logs only error level logs to the console.

    - Example Config:

<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
        </Console>
        <File name="Audit" fileName="./logs/audit.log" immediateFlush="false" append="false">
            <PatternLayout pattern="%d{yyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="Console" />
            <AppenderRef ref="Audit"/>
        </Root>
    </Loggers>
</Configuration>


----------------------
- A few words on MDC -
----------------------

    - Assume you had a class that did some work and you wanted to run it on multiple threads to increase performance.
    - Suppose that you set it up so that a new thread was created for each individual user.
    - Your logger is initiated statically for the class that leveraged across each thread. Your log messages could get chaotic as each thread moves around various parts of your code simultaneously and spits out similar log statements in an overall non-sequential way from the single logger.

    - This is where MDC (Mapped Diagnostic Context) comes in.
    - Really, it's just a big static map/dictionary of key value pairs.
    - One of these Maps is created for every thread that runs in a process. This map is actually not tied directly to your loggers, and you can really put any key/value pair in it at any time, even without instantiating any logger. The MDC data dies with the thread.

    - Why is this useful:
    You can set up your log statement templates to include values that are currently in the MDC.

    - So if you have a log message template pattern like this:
        <Pattern>%X{first} %X{last} - %m%n</Pattern>
    - and you add the corresponding the keys to MDC via static method calls at runtime in a thread:
        MDC.put("first", "Richard");
        MDC.put("last", "Nixon");

    - When you log, your message template will interpolate those values in MDC until they are removed, or updated.
        logger.info("Check enclosed.");
        logger.debug("The most beautiful two words in English.");

    - The above log statements would interpolate to the following in the console and your log files:
        Richard Nixon - I am not a crook.
        Richard Nixon - Attributed to the former US president. 17 Nov 1973.

    ** Credit to logback documentation for the above example.

    - This is a powerful concept if you replace first/last name with something like customerId or userUuid. It provides more context in a templtated way that doesn't involve verbosity in your log statements.

    - The trade off is that you instead have to manage the values you add to the keys in MDC.
