package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.time.LocalDate;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class XpathMetsMix {
    private Integer MixImageHeight;
    private Integer MixImageWidth;
    private String ChecksumMix;
    private String MixFileName;
    public XpathMetsMix(){

    }
    public void setMetsMixInjectedFileData(DecoratedAttributeParsingEvent decoratedEvent,
                                           String injectedType,
                                           String avis,
                                           LocalDate editionDate,
                                           String udgave,
                                           String sectionName,
                                           Integer pageNumber) throws IOException {
        Document document = EventHandlerUtils.handleDocument(decoratedEvent);
        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");

        String fileName = xpath.selectString(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");

        MixFileName = removeExtension(lastName(fileName));

        MixImageHeight = xpath.selectInteger(
                document, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");

        MixImageWidth = xpath.selectInteger(
                document,
                "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");

        ChecksumMix = xpath.selectString(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:Fixity/mix:messageDigest");

    }
    public Integer getMixImageHeight() {
        return MixImageHeight;
    }

    public Integer getMixImageWidth() {
        return MixImageWidth;
    }

    public String getChecksumMix() {
        return ChecksumMix;
    }

    public String getMixFileName() {
        return MixFileName;
    }

}

