package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.util.yaml.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Properties;

public class TiffCheckerExiv2 extends DecoratedEventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TiffCheckerExiv2(ResultCollector resultCollector) {
        super(resultCollector);
    }

    @Override
    public void injectedFile(DecoratedAttributeParsingEvent event,
                             String injectedType,
                             String avis,
                             LocalDate editionDate,
                             String udgave,
                             String sectionName,
                             Integer pageNumber) throws IOException {

        //TODO this should probably be folded into XpathCrossChecker..

        if (!Objects.equals(injectedType, TiffAnalyzerExiv2.INJECTED_TYPE)) {
            return;
        }
        log.info("Checking {}", event.getLocation());
    
    
        Properties properties = new Properties();
        try (InputStream in = event.getData()) {
            properties.load(in);
        }
        
        //Compression -> Uncompressed

        //TODO implement these checks
        //format -> image/tiff
        checkEquals(event,
                    FailureType.INVALID_TIFF_ERROR,
                    "Exiv2 reports invalid format {actual}. Should have been {expected}",
                    properties.getProperty("format"),
                    "image/tiff"
                   );

        // ColorSpace -> Uncalibrated
        checkEquals(event,
                    FailureType.INVALID_TIFF_ERROR,
                    "Exiv2 reports invalid PhotometricInterpretation {actual}. Should have been {expected}",
                    properties.getProperty("PhotometricInterpretation"),
                    "RGB"
                   );
        
        //SamplesPerPixel -> 3
        checkEquals(event,
                    FailureType.INVALID_TIFF_ERROR,
                    "Exiv2 reports invalid Bit depth {actual}. Should have been {expected}",
                    properties.getProperty("BitsPerSample"),
                    "8 8 8"
                   );

        //TODO other tests https://sbprojects.statsbiblioteket.dk/jira/browse/IOF-32
        //TODO test uncompressed https://sbprojects.statsbiblioteket.dk/jira/browse/IOF-32
    }

}
