package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Abstract tree event handler, with no-op methods
 */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private ResultCollector resultCollector;

    public DefaultTreeEventHandler(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    public final ResultCollector getResultCollector() {
        return resultCollector;
    }


    public void handleFinish() throws IOException {
    }

    public void handleNodeBegin(NodeBeginsParsingEvent event) throws IOException {
    }

    public void handleNodeEnd(NodeEndParsingEvent event) throws IOException {
    }

    public void handleAttribute(AttributeParsingEvent event) throws IOException {
    }


    protected void checkEquals(ParsingEvent event,
                               String type,
                               String description,
                               Object actual,
                               Object expected) {
        String actualString = asString(actual);
        String expectedString = asString(expected);
        if (!actualString.equalsIgnoreCase(expectedString)) {
            addFailure(event,
                       type,
                       this.getClass().getSimpleName(),
                       description.replace("{expected}", expectedString).replace("{actual}", actualString));
        }
    }

    protected void checkAllEquals(ParsingEvent event,
                                  String type,
                                  String description,
                                  Object... equals
                                 ) {
        if (Arrays.stream(equals).distinct().count() != 1) {

            String replace = description;
            for (int i = 0; i < equals.length; i++) {
                replace = replace.replace("{val" + i + "}", asString(equals[i]));
            }

            addFailure(event,
                       type,
                       this.getClass().getSimpleName(),
                       replace);
        }

    }

    private String asString(Object object) {
        if (object == null) {
            return "null"; //TODO...
        } else {
            return object.toString();
        }
    }

    protected void checkTrue(ParsingEvent event,
                             String type,
                             String description,
                             boolean bool) {
        if (!bool) {
            addFailure(event,
                       type,
                       this.getClass().getSimpleName(),
                       description);
        }
    }

    protected void checkAtLeast(ParsingEvent event,
                                String type,
                                Double actual,
                                Double required,
                                String description) {
        if (actual < required) {
            addFailure(event,
                       type,
                       this.getClass().getSimpleName(),
                       description.replace("{required}", asString(required)).replace("{actual}", asString(actual)));
        }
    }

    protected void checkWithinRange(ParsingEvent event,
                                    String type,
                                    Double actual,
                                    Double requiredMin,
                                    Double requiredMax,
                                    String description) {

        if (actual < requiredMin || actual > requiredMax) {
            addFailure(event,
                       type,
                       this.getClass().getSimpleName(),
                       description.replace("{requiredMin}", asString(requiredMin))
                                  .replace("{actual}", asString(actual))
                                  .replace("{requiredMax}", asString(requiredMax)));
        }
    }

    protected void reportException(ParsingEvent event, Exception e) {
        addFailure(event,
                   EventRunner.EXCEPTION,
                   this.getClass().getSimpleName(),
                   EventRunner.UNEXPECTED_ERROR + e,
                   Arrays.stream(e.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
    }


    public void addFailure(ParsingEvent fileReference,
                           String type,
                           String component,
                           String description,
                           String... details) {
        log.warn(
                "Adding failure for " +
                "resource '{}' " +
                "of type '{}' " +
                "from component '{}' " +
                "with description '{}' " +
                "and details '{}'", fileReference, type, component, description, String.join("\n", details));

        getResultCollector().addFailure(fileReference, type, component, description, details);
    }

}
