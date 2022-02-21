package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.util.yaml.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;

public class TiffChecker extends DecoratedEventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public TiffChecker(ResultCollector resultCollector) {
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
        if (!Objects.equals(injectedType, TiffAnalyzer.INJECTED_TYPE)){
            return;
        }
        log.info("Checking {}", event.getLocation());
    
    
        YAML result;
        try (InputStream in = event.getData()) {
            result = YAML.parse(in);
        }
        YAML yaml = result;
        
        //See src/test/resources/sampleImageMagickOutput.yaml for what and how
        
        checkEquals(event,
                    "INVALID_TIFF", "ImageMagick reports invalid format {actual}. Should have been {expected}", yaml.getString("Image.Format"),
                    "TIFF (Tagged Image File Format)"
        );
        
        checkEquals(event,
                    "INVALID_TIFF", "ImageMagick reports invalid Colorspace {actual}. Should have been {expected}", yaml.getString("Image.Colorspace"),
                    "sRGB"
        );
        
        checkEquals(event,
                    "INVALID_TIFF", "ImageMagick reports invalid Bit depth {actual}. Should have been {expected}", yaml.getString("Image.Depth"),
                    "8-bit"
        );
        
        //TODO other tests
    }
    
}
