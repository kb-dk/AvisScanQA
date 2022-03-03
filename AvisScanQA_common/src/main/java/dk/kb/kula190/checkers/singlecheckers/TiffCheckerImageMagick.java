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

public class TiffCheckerImageMagick extends DecoratedEventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TiffCheckerImageMagick(ResultCollector resultCollector) {
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

        if (!Objects.equals(injectedType, TiffAnalyzerImageMagick.INJECTED_TYPE)) {
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
                    FailureType.INVALID_TIFF_ERROR,
                    "ImageMagick reports invalid format {actual}. Should have been {expected}",
                    yaml.getString("Image.Format"),
                    "TIFF (Tagged Image File Format)"
                   );

        checkEquals(event,
                    FailureType.INVALID_TIFF_ERROR,
                    "ImageMagick reports invalid Colorspace {actual}. Should have been {expected}",
                    yaml.getString("Image.Colorspace"),
                    "sRGB"
                   );

        checkEquals(event,
                    FailureType.INVALID_TIFF_ERROR,
                    "ImageMagick reports invalid Bit depth {actual}. Should have been {expected}",
                    yaml.getString("Image.Depth"),
                    "8-bit"
                   );

        //TODO other tests https://sbprojects.statsbiblioteket.dk/jira/browse/IOF-32
        //TODO test uncompressed https://sbprojects.statsbiblioteket.dk/jira/browse/IOF-32
    }

}
