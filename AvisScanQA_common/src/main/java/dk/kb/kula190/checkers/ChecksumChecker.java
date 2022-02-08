package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ChecksumChecker extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    
    public ChecksumChecker(ResultCollector resultCollector) {this.resultCollector = resultCollector;}
    
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith(".xml")) { //is XML File
            String computedMD5;
            try (InputStream data = event.getData()) {
                computedMD5 = DigestUtils.md5Hex(data);
                String givenMD5 = event.getChecksum();
                if (! computedMD5.equalsIgnoreCase(givenMD5)){
                    resultCollector.addFailure(event.getName(), "ChecksumMismatch", this.getClass().getSimpleName(), "File have checksum "+computedMD5+" but should have checksum "+givenMD5 );
                }
            } catch (IOException e) {
                resultCollector.addFailure(event.getName(),
                                           EventRunner.EXCEPTION,
                                           this.getClass().getSimpleName(),
                                           EventRunner.UNEXPECTED_ERROR + e,
                                           Arrays.stream(e.getStackTrace())
                                                 .map(StackTraceElement::toString)
                                                 .collect(Collectors.joining("\n")));
                return;
            }
            
            
        }
    }
}
