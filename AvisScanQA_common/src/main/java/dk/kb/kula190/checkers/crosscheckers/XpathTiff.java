package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.checkers.singlecheckers.TiffAnalyzer;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.yaml.YAML;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * @see XpathCrossChecker
 */
public class XpathTiff {

    private Integer ImageHeightTif;
    private Integer ImageWidthTif;
    private String TifFileName;
    private String ChecksumTif;
    private Integer TifSizeActual;

    public XpathTiff(){
    }

    public void setTiffXpathData(DecoratedAttributeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        TifSizeActual = (int) new File(event.getLocation()).length();

        String tiffFileName = removeExtension(lastName(event.getLocation()));
        TifFileName = tiffFileName;

        ChecksumTif = event.getChecksum();
    }
    public void setTiffInjectedFileData(DecoratedAttributeParsingEvent decoratedEvent, String injectedType, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        if (!Objects.equals(injectedType, TiffAnalyzer.INJECTED_TYPE)){
            return;
        }
        YAML result;
        try (InputStream in = decoratedEvent.getData()) {
            result = YAML.parse(in);
        }
        YAML yaml = result;
        //See src/test/resources/sampleImageMagickOutput.yaml for what and how

        String geo = yaml.getString("Image.Geometry").split("\\+", 2)[0];
        String[] geoSplits = geo.split("x");

        ImageWidthTif = Integer.getInteger(geoSplits[0]);
        ImageHeightTif = Integer.getInteger(geoSplits[1]);
    }
    public Integer getImageHeightTif() {
        return ImageHeightTif;
    }

    public Integer getImageWidthTif() {
        return ImageWidthTif;
    }

    public String getTifFileName() {
        return TifFileName;
    }

    public String getChecksumTif() {
        return ChecksumTif;
    }

    public Integer getTifSizeActual() {
        return TifSizeActual;
    }


}
