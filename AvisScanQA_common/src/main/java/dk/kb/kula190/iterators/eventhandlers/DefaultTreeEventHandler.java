package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;

import javax.activation.DataHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Abstract tree event handler, with no-op methods
 */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {
    
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
                               String actual,
                               String expected,
                               String description) {
        if (!actual.equalsIgnoreCase(expected)) {
            getResultCollector().addFailure(event.getLocation(),
                                            type,
                                            this.getClass().getSimpleName(),
                                            description.replace("{expected}", expected).replace("{actual}", actual));
        }
    }
    
    
    
    protected void checkAtLeast(ParsingEvent event,
                                String type,
                                Double actual,
                                Double required,
                                String description) {
        if (actual < required) {
            getResultCollector().addFailure(event.getLocation(),
                                            type,
                                            this.getClass().getSimpleName(),
                                            description.replace("{required}", required.toString()).replace("{actual}", actual.toString()));
        }
    }
    
    protected void reportException(ParsingEvent event, Exception e){
        getResultCollector().addFailure(event.getName(),
                                        EventRunner.EXCEPTION,
                                        this.getClass().getSimpleName(),
                                        EventRunner.UNEXPECTED_ERROR + e,
                                        Arrays.stream(e.getStackTrace())
                                              .map(StackTraceElement::toString)
                                              .collect(Collectors.joining("\n")));
    }
    
}
